package com.upismart.backend.controller;

import com.upismart.backend.dto.BillResponse;
import com.upismart.backend.dto.InsightResponse;
import com.upismart.backend.dto.SalaryUtilizationResponse;
import com.upismart.backend.entity.TransactionEntity;
import com.upismart.backend.service.BillService;
import com.upismart.backend.service.BudgetService;
import com.upismart.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
public class InsightController {

    private final BudgetService budgetService;
    private final PaymentService paymentService;
    private final BillService billService;

    public InsightController(BudgetService budgetService, PaymentService paymentService, BillService billService) {
        this.budgetService = budgetService;
        this.paymentService = paymentService;
        this.billService = billService;
    }

    @GetMapping
    public ResponseEntity<InsightResponse> getInsights() {
        SalaryUtilizationResponse budget = budgetService.getSalaryUtilization();
        List<TransactionEntity> transactions = paymentService.getAllTransactions();
        List<BillResponse> bills = billService.getBillsForUser();

        BigDecimal billsReserved = bills.stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsPaid()))
                .map(BillResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find top category and largest debit
        Map<String, BigDecimal> catTotals = new HashMap<>();
        String largestDebitTitle = "None yet";
        BigDecimal largestDebitAmount = BigDecimal.ZERO;

        for (TransactionEntity tx : transactions) {
            catTotals.put(tx.getCategory(), catTotals.getOrDefault(tx.getCategory(), BigDecimal.ZERO).add(tx.getAmount()));
            if (tx.getAmount().compareTo(largestDebitAmount) > 0) {
                largestDebitAmount = tx.getAmount();
                largestDebitTitle = tx.getTitle() != null && !tx.getTitle().isEmpty() ? tx.getTitle() : tx.getRecipientName();
            }
        }

        String topCat = "None";
        BigDecimal topCatAmount = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : catTotals.entrySet()) {
            if (entry.getValue().compareTo(topCatAmount) > 0) {
                topCat = entry.getKey();
                topCatAmount = entry.getValue();
            }
        }

        double velocity = budget.getPercentageSpent();
        boolean isVelocityWarning = velocity >= 80.0;

        List<String> detections = new ArrayList<>();
        if (isVelocityWarning) {
            detections.add(String.format("High Velocity Warning: You've consumed %.1f%% of your monthly budget.", velocity));
        }
        if (topCatAmount.compareTo(BigDecimal.ZERO) > 0) {
            detections.add(String.format("Dominant Outflow: '%s' represents the highest expenditure category (₹%.2f).", topCat, topCatAmount));
        }
        if (largestDebitAmount.compareTo(budget.getMonthlyIncome().multiply(new BigDecimal("0.20"))) >= 0) {
            detections.add(String.format("High Impact Transaction: '%s' of ₹%.2f exceeded 20%% of monthly income.", largestDebitTitle, largestDebitAmount));
        }
        if (detections.isEmpty()) {
            detections.add("Safe Spending Velocity: All outflows within comfortable deterministic limits.");
        }

        List<String> bucksTips = new ArrayList<>();
        if (velocity < 40.0) {
            bucksTips.add("Yo, your Safe-to-Spend pace is elite! Looking fresh for the end of the month.");
            bucksTips.add("Pro tip: Stash extra unspent funds into your Emergency Vault!");
        } else if (velocity < 80.0) {
            bucksTips.add("Pacing is chill right now, but keep an eye on upcoming bills.");
            bucksTips.add(String.format("You got ₹%.2f reserved for unpaid bills this cycle.", billsReserved));
        } else {
            bucksTips.add("Slow down boss! You're in the red zone for this month.");
            bucksTips.add("Hold off on impulse shopping and stick strictly to the daily Safe-to-Spend limit.");
        }

        InsightResponse response = new InsightResponse(
                budget.getMonthlyIncome(),
                budget.getTotalExpenditure(),
                budget.getRemainingBalance(),
                budget.getDailySafeToSpend(),
                billsReserved,
                topCat,
                topCatAmount,
                largestDebitTitle,
                largestDebitAmount,
                velocity,
                isVelocityWarning,
                detections,
                bucksTips
        );

        return ResponseEntity.ok(response);
    }
}
