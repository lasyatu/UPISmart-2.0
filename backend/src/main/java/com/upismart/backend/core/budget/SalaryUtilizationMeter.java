package com.upismart.backend.core.budget;

import com.upismart.backend.core.model.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulated Core Java business logic for the 100% Salary-Utilization Meter.
 *
 * Mathematical formulas:
 *   Percentage Spent = (Total Expenditure / Monthly Income) * 100
 *   Remaining Balance = Monthly Income - Total Expenditure
 *   Percentage Remaining = (Remaining Balance / Monthly Income) * 100
 *
 * Handles zero income, invalid amounts, and over-spending safely.
 */
public class SalaryUtilizationMeter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double monthlyIncome;
    private final double totalExpenditure;
    private final double committedBills;
    private final Map<String, Double> categoryBreakdown;

    public SalaryUtilizationMeter(double monthlyIncome, double totalExpenditure, double committedBills, Map<String, Double> categoryBreakdown) {
        this.monthlyIncome = Math.max(0.0, monthlyIncome);
        this.totalExpenditure = Math.max(0.0, totalExpenditure);
        this.committedBills = Math.max(0.0, committedBills);
        this.categoryBreakdown = categoryBreakdown != null ? new HashMap<>(categoryBreakdown) : Collections.emptyMap();
    }

    public static SalaryUtilizationMeter calculate(double monthlyIncome, List<Transaction> transactions, double committedBills) {
        double total = 0.0;
        Map<String, Double> breakdown = new HashMap<>();

        if (transactions != null) {
            for (Transaction tx : transactions) {
                if (tx != null && "SUCCESS".equalsIgnoreCase(tx.getStatus()) && tx.getAmount() > 0) {
                    total += tx.getAmount();
                    String cat = tx.getCategory() != null ? tx.getCategory() : "Other";
                    breakdown.put(cat, breakdown.getOrDefault(cat, 0.0) + tx.getAmount());
                }
            }
        }

        return new SalaryUtilizationMeter(monthlyIncome, total, committedBills, breakdown);
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getTotalExpenditure() {
        return totalExpenditure;
    }

    public double getCommittedBills() {
        return committedBills;
    }

    public double getRemainingBalance() {
        if (monthlyIncome <= 0.0) return 0.0;
        return monthlyIncome - totalExpenditure;
    }

    public double getPercentageSpent() {
        if (monthlyIncome <= 0.0) return 0.0;
        BigDecimal spent = BigDecimal.valueOf(totalExpenditure);
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        return spent.multiply(BigDecimal.valueOf(100.0))
                .divide(income, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public double getPercentageRemaining() {
        if (monthlyIncome <= 0.0) return 0.0;
        double remaining = getRemainingBalance();
        if (remaining <= 0.0) return 0.0;
        BigDecimal rem = BigDecimal.valueOf(remaining);
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        return rem.multiply(BigDecimal.valueOf(100.0))
                .divide(income, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public String getUtilizationStatus() {
        double pct = getPercentageSpent();
        if (pct >= 100.0) return "EXCEEDED";
        if (pct >= 85.0) return "CRITICAL";
        if (pct >= 70.0) return "WARNING";
        if (pct >= 40.0) return "MODERATE";
        return "OPTIMAL";
    }

    public Map<String, Double> getCategoryBreakdown() {
        return Collections.unmodifiableMap(categoryBreakdown);
    }
}
