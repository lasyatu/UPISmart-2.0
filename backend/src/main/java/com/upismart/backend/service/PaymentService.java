package com.upismart.backend.service;

import com.upismart.backend.core.budget.BudgetManager;
import com.upismart.backend.core.detector.UnusualSpendingDetector;
import com.upismart.backend.core.exception.*;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.PaymentRequest;
import com.upismart.backend.core.model.PaymentResult;
import com.upismart.backend.core.payment.DemoUpiPaymentProcessor;
import com.upismart.backend.core.payment.PaymentProcessor;
import com.upismart.backend.dto.PaymentApiRequest;
import com.upismart.backend.dto.PaymentApiResponse;
import com.upismart.backend.entity.EmergencyVaultEntity;
import com.upismart.backend.entity.TransactionEntity;
import com.upismart.backend.entity.UserEntity;
import com.upismart.backend.repository.EmergencyVaultRepository;
import com.upismart.backend.repository.TransactionRecordRepository;
import com.upismart.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final TransactionRecordRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmergencyVaultRepository vaultRepository;
    private final PaymentProcessor paymentProcessor;
    private final UnusualSpendingDetector unusualSpendingDetector;

    public PaymentService(TransactionRecordRepository transactionRepository,
                          UserRepository userRepository,
                          EmergencyVaultRepository vaultRepository,
                          @Value("${app.demo.demo-pin:1234}") String demoPin) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.vaultRepository = vaultRepository;
        this.unusualSpendingDetector = new UnusualSpendingDetector();
        this.paymentProcessor = new DemoUpiPaymentProcessor(demoPin, unusualSpendingDetector);
    }

    @Transactional
    public PaymentApiResponse executePayment(PaymentApiRequest apiRequest)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   InvalidPinException,
                   InvalidPaymentDetailsException,
                   UnusualSpendingAlertException {

        // 1. Get or create current active demo user
        UserEntity user = getOrCreateDemoUser();

        // 2. Fetch existing transaction history to calculate current expenditure
        List<TransactionEntity> pastTxs = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());
        double totalSpent = pastTxs.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // 3. Fetch emergency vault balance to enforce isolated reserve protection
        double vaultReserve = vaultRepository.findByUserId(user.getUserId())
                .map(v -> v.getReserveBalance().doubleValue())
                .orElse(15000.0);

        // 4. Calculate available balance using OOP AccountBalance
        double salary = user.getMonthlySalary().doubleValue();
        AccountBalance balance = new AccountBalance(salary, totalSpent, 0.0, vaultReserve);

        // 5. Build core OOP PaymentRequest
        PaymentRequest coreRequest = new PaymentRequest(
                apiRequest.getRecipientUpi(),
                apiRequest.getRecipientName() != null ? apiRequest.getRecipientName() : "Demo Recipient",
                apiRequest.getAmount().doubleValue(),
                apiRequest.getCategory() != null ? apiRequest.getCategory() : "Other",
                apiRequest.getEnteredPin() != null ? apiRequest.getEnteredPin() : "",
                apiRequest.getNote() != null ? apiRequest.getNote() : "",
                Boolean.TRUE.equals(apiRequest.getUserConfirmed())
        );

        // 6. Execute polymorphic payment processor
        PaymentResult result = paymentProcessor.processPayment(coreRequest, balance);

        // 7. Persist to relational database (MySQL / Cloud SQL)
        TransactionEntity entity = new TransactionEntity(
                user.getUserId(),
                result.getReferenceId(),
                result.getTransaction().getTitle(),
                result.getTransaction().getRecipientUpi(),
                result.getTransaction().getRecipientName(),
                BigDecimal.valueOf(result.getTransaction().getAmount()),
                result.getTransaction().getCategory(),
                result.getTransaction().getStatus(),
                result.getTransaction().getNote(),
                true
        );
        TransactionEntity saved = transactionRepository.save(entity);

        return new PaymentApiResponse(
                true,
                saved.getReferenceId(),
                result.getMessage(),
                saved.getAmount(),
                saved.getRecipientUpi(),
                saved.getRecipientName(),
                saved.getCategory(),
                BigDecimal.valueOf(result.getRemainingSafeToSpend()),
                saved.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    public List<TransactionEntity> getAllTransactions() {
        UserEntity user = getOrCreateDemoUser();
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());
    }

    public UserEntity getOrCreateDemoUser() {
        return userRepository.findByUpiHandle("alex@demo.upismart")
                .orElseGet(() -> userRepository.save(new UserEntity(
                        "Alex Rivera",
                        "alex@demo.upismart",
                        "alex@demo.upismart",
                        BigDecimal.valueOf(50000.00),
                        "SHA256_SALTED_DEMO_HASH",
                        true
                )));
    }
}
