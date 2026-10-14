package com.upismart.backend.service;

import com.upismart.backend.core.budget.BudgetManager;
import com.upismart.backend.core.budget.SalaryUtilizationMeter;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.model.Transaction;
import com.upismart.backend.dto.SalaryUpdateRequest;
import com.upismart.backend.dto.SalaryUtilizationResponse;
import com.upismart.backend.entity.EmergencyVaultEntity;
import com.upismart.backend.entity.TransactionEntity;
import com.upismart.backend.entity.UserEntity;
import com.upismart.backend.repository.EmergencyVaultRepository;
import com.upismart.backend.repository.TransactionRecordRepository;
import com.upismart.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;
    private final EmergencyVaultRepository vaultRepository;
    private final PaymentService paymentService;

    public BudgetService(UserRepository userRepository,
                         TransactionRecordRepository transactionRepository,
                         EmergencyVaultRepository vaultRepository,
                         PaymentService paymentService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.vaultRepository = vaultRepository;
        this.paymentService = paymentService;
    }

    public SalaryUtilizationResponse getSalaryUtilization() {
        UserEntity user = paymentService.getOrCreateDemoUser();
        List<TransactionEntity> entities = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

        List<Transaction> coreTransactions = entities.stream()
                .map(e -> new Transaction(
                        e.getReferenceId(),
                        e.getTitle(),
                        e.getRecipientUpi(),
                        e.getRecipientName(),
                        e.getAmount().doubleValue(),
                        e.getCategory(),
                        e.getCreatedAt() != null ? e.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis(),
                        e.getStatus(),
                        e.getNote(),
                        Boolean.TRUE.equals(e.getIsSimulated())
                ))
                .collect(Collectors.toList());

        double salary = user.getMonthlySalary().doubleValue();
        double vaultReserve = vaultRepository.findByUserId(user.getUserId())
                .map(v -> v.getReserveBalance().doubleValue())
                .orElse(15000.0);

        BudgetManager budgetManager = new BudgetManager(salary, 0.0, vaultReserve);
        SalaryUtilizationMeter meter = budgetManager.computeUtilization(coreTransactions);

        // Days remaining in current month
        Calendar cal = Calendar.getInstance();
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int daysRemaining = Math.max(1, maxDays - currentDay + 1);

        double safeToSpend = budgetManager.calculateDailySafeToSpend(meter.getTotalExpenditure(), daysRemaining);

        return new SalaryUtilizationResponse(
                BigDecimal.valueOf(meter.getMonthlyIncome()),
                BigDecimal.valueOf(meter.getTotalExpenditure()),
                BigDecimal.valueOf(meter.getRemainingBalance()),
                meter.getPercentageSpent(),
                meter.getPercentageRemaining(),
                meter.getUtilizationStatus(),
                BigDecimal.valueOf(safeToSpend).setScale(2, RoundingMode.HALF_UP),
                meter.getCategoryBreakdown()
        );
    }

    @Transactional
    public SalaryUtilizationResponse updateMonthlySalary(SalaryUpdateRequest request) throws InvalidAmountException {
        if (request.getMonthlySalary() == null || request.getMonthlySalary().doubleValue() <= 0) {
            throw new InvalidAmountException("Monthly income must be strictly greater than 0.",
                    request.getMonthlySalary() != null ? request.getMonthlySalary().doubleValue() : 0.0);
        }

        UserEntity user = paymentService.getOrCreateDemoUser();
        user.setMonthlySalary(request.getMonthlySalary());
        userRepository.save(user);

        return getSalaryUtilization();
    }
}
