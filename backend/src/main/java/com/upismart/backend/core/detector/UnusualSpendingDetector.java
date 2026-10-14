package com.upismart.backend.core.detector;

import com.upismart.backend.core.model.Transaction;

import java.util.List;

/**
 * Transparent, deterministic rule-based spending detector.
 *
 * Checks proposed payments against configurable thresholds and historical averages:
 * Rule 1: High Transaction Alert — Exceeds configured percentage of monthly salary (default 20%).
 * Rule 2: Multiple of Historical Average — Exceeds 3x average historical transaction size.
 * Rule 3: Extreme Flat Amount Alert — Payments greater than or equal to ₹15,000.
 */
public class UnusualSpendingDetector {

    public static final double DEFAULT_SALARY_PERCENT_THRESHOLD = 0.20; // 20% of salary
    public static final double DEFAULT_FLAT_AMOUNT_THRESHOLD = 15000.0;  // ₹15,000

    private final double salaryThresholdRatio;
    private final double flatThreshold;

    public UnusualSpendingDetector() {
        this(DEFAULT_SALARY_PERCENT_THRESHOLD, DEFAULT_FLAT_AMOUNT_THRESHOLD);
    }

    public UnusualSpendingDetector(double salaryThresholdRatio, double flatThreshold) {
        this.salaryThresholdRatio = salaryThresholdRatio > 0 ? salaryThresholdRatio : DEFAULT_SALARY_PERCENT_THRESHOLD;
        this.flatThreshold = flatThreshold > 0 ? flatThreshold : DEFAULT_FLAT_AMOUNT_THRESHOLD;
    }

    public SpendingRuleResult evaluate(double attemptedAmount, double monthlySalary, List<Transaction> recentTransactions) {
        if (attemptedAmount <= 0) {
            return SpendingRuleResult.normal();
        }

        // Rule 1: Single transaction exceeds threshold % of monthly income
        if (monthlySalary > 0) {
            double salaryLimit = monthlySalary * salaryThresholdRatio;
            if (attemptedAmount > salaryLimit) {
                int percent = (int) Math.round(salaryThresholdRatio * 100);
                return SpendingRuleResult.alert(
                        "SALARY_THRESHOLD_EXCEEDED",
                        String.format("Unusual Spending Alert: ₹%.2f exceeds %d%% of your monthly income (₹%.2f).",
                                attemptedAmount, percent, salaryLimit),
                        salaryLimit
                );
            }
        }

        // Rule 2: Large flat threshold alert
        if (attemptedAmount >= flatThreshold) {
            return SpendingRuleResult.alert(
                    "LARGE_TRANSACTION_LIMIT",
                    String.format("Unusual Spending Alert: Single transaction of ₹%.2f is unusually large.",
                            attemptedAmount),
                    flatThreshold
            );
        }

        // Rule 3: Check against historical average (if history exists)
        if (recentTransactions != null && !recentTransactions.isEmpty()) {
            double sum = 0.0;
            int count = 0;
            for (Transaction tx : recentTransactions) {
                if (tx.getAmount() > 0) {
                    sum += tx.getAmount();
                    count++;
                }
            }
            if (count >= 3) {
                double avg = sum / count;
                double multiplierLimit = avg * 3.0;
                if (attemptedAmount > multiplierLimit && attemptedAmount > 2000.0) {
                    return SpendingRuleResult.alert(
                            "HISTORICAL_AVERAGE_SPIKE",
                            String.format("Unusual Spending Alert: ₹%.2f is more than 3x your average payment of ₹%.2f.",
                                    attemptedAmount, avg),
                            multiplierLimit
                    );
                }
            }
        }

        return SpendingRuleResult.normal();
    }
}
