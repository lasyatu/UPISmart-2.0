package com.upismart.backend.service;

import com.upismart.backend.dto.BillRequest;
import com.upismart.backend.dto.BillResponse;
import com.upismart.backend.dto.PaymentApiRequest;
import com.upismart.backend.entity.BillEntity;
import com.upismart.backend.entity.UserEntity;
import com.upismart.backend.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final PaymentService paymentService;

    public BillService(BillRepository billRepository, PaymentService paymentService) {
        this.billRepository = billRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public List<BillResponse> getBillsForUser() {
        UserEntity user = paymentService.getOrCreateDemoUser();
        List<BillEntity> bills = billRepository.findByUserIdOrderByDueDayAsc(user.getUserId());

        if (bills.isEmpty()) {
            // Seed initial Bills Radar obligations for demo
            seedInitialBills(user.getUserId());
            bills = billRepository.findByUserIdOrderByDueDayAsc(user.getUserId());
        }

        int currentDay = LocalDate.now().getDayOfMonth();

        return bills.stream().map(b -> {
            boolean isOverdue = !Boolean.TRUE.equals(b.getIsPaid()) && b.getDueDay() < currentDay;
            return new BillResponse(
                    b.getBillId(),
                    b.getUserId(),
                    b.getTitle(),
                    b.getCategory(),
                    b.getAmount(),
                    b.getDueDay(),
                    b.getIsPaid(),
                    b.getIsRecurring(),
                    b.getBillerUpi(),
                    b.getLastPaidAt(),
                    isOverdue
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public BillResponse createBill(BillRequest request) {
        UserEntity user = paymentService.getOrCreateDemoUser();
        BillEntity bill = new BillEntity(
                user.getUserId(),
                request.getTitle(),
                request.getCategory(),
                request.getAmount(),
                request.getDueDay(),
                false
        );
        bill.setBillerUpi(request.getBillerUpi());
        BillEntity saved = billRepository.save(bill);

        return new BillResponse(
                saved.getBillId(),
                saved.getUserId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getAmount(),
                saved.getDueDay(),
                saved.getIsPaid(),
                saved.getIsRecurring(),
                saved.getBillerUpi(),
                saved.getLastPaidAt(),
                false
        );
    }

    @Transactional
    public BillResponse payBill(Long billId) {
        BillEntity bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + billId));

        bill.setIsPaid(true);
        bill.setLastPaidAt(LocalDateTime.now());
        BillEntity updated = billRepository.save(bill);

        // Also record a simulated payment in transaction history
        try {
            PaymentApiRequest tx = new PaymentApiRequest(
                    bill.getBillerUpi() != null ? bill.getBillerUpi() : "bills@demo.upismart",
                    bill.getTitle(),
                    bill.getAmount(),
                    bill.getCategory(),
                    "1234",
                    "Simulated Bill Payment: " + bill.getTitle(),
                    Boolean.TRUE
            );
            paymentService.executePayment(tx);
        } catch (Exception ignored) {
            // Bill is marked paid even if transaction recording faces rule warning
        }

        return new BillResponse(
                updated.getBillId(),
                updated.getUserId(),
                updated.getTitle(),
                updated.getCategory(),
                updated.getAmount(),
                updated.getDueDay(),
                updated.getIsPaid(),
                updated.getIsRecurring(),
                updated.getBillerUpi(),
                updated.getLastPaidAt(),
                false
        );
    }

    private void seedInitialBills(Long userId) {
        List<BillEntity> initial = new ArrayList<>();
        initial.add(new BillEntity(userId, "Fiber High-Speed Internet", "Bills & Utilities", new BigDecimal("1299.00"), 5, false));
        initial.add(new BillEntity(userId, "Electricity / Power Grid", "Bills & Utilities", new BigDecimal("2450.00"), 12, false));
        initial.add(new BillEntity(userId, "Cloud Storage & Streaming", "Subscriptions", new BigDecimal("799.00"), 20, false));
        initial.add(new BillEntity(userId, "Apartment Maintenance", "Housing", new BigDecimal("3500.00"), 25, false));
        billRepository.saveAll(initial);
    }
}
