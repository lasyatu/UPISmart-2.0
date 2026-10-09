package com.example.core.budget;

import com.example.core.model.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable Core Java business logic calculating the 100% Salary-Utilization Meter.
 *
 * Formulas:
 * Percentage spent = (Total expenditure / Monthly income) * 100
 * Remaining balance = Monthly income - Total expenditure
 * Percentage remaining = 100.0 - Percentage spent
 *
 * Implements defensive checks for zero income, negative/invalid amounts, and over-budget expenditures.
 */
public class SalaryUtilizationMeter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double monthlyIncome;
    private final double totalExpenditure;
    private final double remainingBalance;
    private final double percentageSpent;
    private final double percentageRemaining;
    private final boolean isOverBudget;
    private final Map<String, Double> expensesByCategory;

    public SalaryUtilizationMeter(double monthlyIncome, double totalExpenditure, Map<String, Double> categoryMap) {
        // Handle invalid/negative amounts safely
        this.monthlyIncome = Math.max(0.0, monthlyIncome);
        this.totalExpenditure = Math.max(0.0, totalExpenditure);

        // Remaining balance = Monthly income - Total expenditure
        BigDecimal incomeBD = BigDecimal.valueOf(this.monthlyIncome);
        BigDecimal expenseBD = BigDecimal.valueOf(this.totalExpenditure);
        this.remainingBalance = incomeBD.subtract(expenseBD).setScale(2, RoundingMode.HALF_EVEN).doubleValue();

        // Percentage spent = (Total expenditure / Monthly income) * 100
        if (this.monthlyIncome > 0.0) {
            BigDecimal spentRatio = expenseBD.divide(incomeBD, 6, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100.0));
            this.percentageSpent = spentRatio.setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            this.percentageRemaining = Math.max(0.0, 100.0 - this.percentageSpent);
        } else {
            // Zero income safe fallback: avoid ArithmeticException divide-by-zero
            this.percentageSpent = this.totalExpenditure > 0.0 ? 100.0 : 0.0;
            this.percentageRemaining = 0.0;
        }

        this.isOverBudget = this.totalExpenditure > this.monthlyIncome;
        this.expensesByCategory = categoryMap != null ?
                Collections.unmodifiableMap(new HashMap<>(categoryMap)) :
                Collections.emptyMap();
    }

    /**
     * Factory method computing the SalaryUtilizationMeter from raw transaction history and monthly salary.
     */
    public static SalaryUtilizationMeter compute(double monthlyIncome, List<Transaction> transactions) {
        double totalExpense = 0.0;
        Map<String, Double> categoryTotals = new HashMap<>();

        if (transactions != null) {
            for (Transaction tx : transactions) {
                if (tx != null && "SUCCESS".equalsIgnoreCase(tx.getStatus()) && tx.getAmount() > 0) {
                    double amt = tx.getAmount();
                    totalExpense += amt;

                    String cat = (tx.getCategory() != null && !tx.getCategory().trim().isEmpty()) ?
                            tx.getCategory().trim() : "Other";
                    categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + amt);
                }
            }
        }

        return new SalaryUtilizationMeter(monthlyIncome, totalExpense, categoryTotals);
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getTotalExpenditure() {
        return totalExpenditure;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public double getPercentageSpent() {
        return percentageSpent;
    }

    public double getPercentageRemaining() {
        return percentageRemaining;
    }

    public boolean isOverBudget() {
        return isOverBudget;
    }

    public Map<String, Double> getExpensesByCategory() {
        return expensesByCategory;
    }

    @Override
    public String toString() {
        return "SalaryUtilizationMeter{" +
                "income=" + monthlyIncome +
                ", expenditure=" + totalExpenditure +
                ", remaining=" + remainingBalance +
                ", spent=" + percentageSpent + "%" +
                ", remainingPct=" + percentageRemaining + "%" +
                ", isOverBudget=" + isOverBudget +
                '}';
    }
}
