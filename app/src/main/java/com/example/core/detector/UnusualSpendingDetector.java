package com.example.core.detector;

import com.example.core.model.Transaction;

import java.util.List;

/**
 * Transparent, deterministic rule-based spending detector.
 *
 * Checks proposed payments against configurable thresholds and historical spending averages.
 * Note: Strictly deterministic expert rules, NOT machine learning.
 */
public class UnusualSpendingDetector {

    // Default threshold: Single payment >= 20% of monthly budget triggers high-impact alert
    private double highImpactBudgetPercentageThreshold = 0.20;

    // Default threshold: Payment >= 3.0x the average transaction amount triggers anomaly alert
    private double historicalAverageMultiplierThreshold = 3.0;

    public UnusualSpendingDetector() {
    }

    public UnusualSpendingDetector(double highImpactBudgetPercentageThreshold, double historicalAverageMultiplierThreshold) {
        this.highImpactBudgetPercentageThreshold = highImpactBudgetPercentageThreshold;
        this.historicalAverageMultiplierThreshold = historicalAverageMultiplierThreshold;
    }

    public double getHighImpactBudgetPercentageThreshold() {
        return highImpactBudgetPercentageThreshold;
    }

    public void setHighImpactBudgetPercentageThreshold(double highImpactBudgetPercentageThreshold) {
        this.highImpactBudgetPercentageThreshold = highImpactBudgetPercentageThreshold;
    }

    public double getHistoricalAverageMultiplierThreshold() {
        return historicalAverageMultiplierThreshold;
    }

    public void setHistoricalAverageMultiplierThreshold(double historicalAverageMultiplierThreshold) {
        this.historicalAverageMultiplierThreshold = historicalAverageMultiplierThreshold;
    }

    /**
     * Checks if a proposed payment triggers any rule-based spending alerts prior to confirmation.
     *
     * @param proposedAmount The amount intended to be paid.
     * @param monthlyBudget The monthly total income/budget cap.
     * @param remainingBudget The current unspent buffer in the monthly cycle.
     * @param transactionHistory Past transactions used to evaluate typical spending patterns.
     * @return SpendingRuleResult describing if flagged and why.
     */
    public SpendingRuleResult evaluatePrePayment(double proposedAmount,
                                                 double monthlyBudget,
                                                 double remainingBudget,
                                                 List<Transaction> transactionHistory) {
        if (proposedAmount <= 0) {
            return SpendingRuleResult.clean();
        }

        // Rule 1: Payment exceeds remaining monthly budget
        if (monthlyBudget > 0 && proposedAmount > remainingBudget) {
            return new SpendingRuleResult(
                    true,
                    SpendingRuleResult.RuleType.EXCEEDS_REMAINING_BUDGET,
                    "Budget Overrun Warning",
                    String.format("Payment of ₹%.2f exceeds your remaining unreserved budget buffer of ₹%.2f.",
                            proposedAmount, remainingBudget),
                    "Rule: proposedAmount > remainingBudget. Approving this debit will push total expenditure beyond monthly income allocation."
            );
        }

        // Rule 2: Single payment >= high impact threshold (e.g. 20% of total budget)
        if (monthlyBudget > 0 && proposedAmount >= (monthlyBudget * highImpactBudgetPercentageThreshold)) {
            double percent = (proposedAmount / monthlyBudget) * 100.0;
            return new SpendingRuleResult(
                    true,
                    SpendingRuleResult.RuleType.HIGH_BUDGET_PERCENTAGE,
                    "High Impact Payment Alert",
                    String.format("This single payment of ₹%.2f consumes %.1f%% of your entire monthly budget (₹%.2f).",
                            proposedAmount, percent, monthlyBudget),
                    String.format("Rule: proposedAmount >= %.0f%% of monthly budget. Large lump-sum payments require active user awareness.",
                            highImpactBudgetPercentageThreshold * 100)
            );
        }

        // Rule 3: Single payment >= multiplier of historical average transaction
        if (transactionHistory != null && !transactionHistory.isEmpty()) {
            double totalHistorical = 0.0;
            int count = 0;
            for (Transaction tx : transactionHistory) {
                if ("SUCCESS".equalsIgnoreCase(tx.getStatus()) && tx.getAmount() > 0) {
                    totalHistorical += tx.getAmount();
                    count++;
                }
            }

            if (count >= 3) {
                double avgAmount = totalHistorical / count;
                if (proposedAmount >= (avgAmount * historicalAverageMultiplierThreshold)) {
                    double multiplier = proposedAmount / avgAmount;
                    return new SpendingRuleResult(
                            true,
                            SpendingRuleResult.RuleType.MULTIPLIER_OF_AVERAGE,
                            "Unusually High Transaction",
                            String.format("This payment (₹%.2f) is %.1fx higher than your average debit of ₹%.2f.",
                                    proposedAmount, multiplier, avgAmount),
                            String.format("Rule: proposedAmount >= %.1fx mean past transaction (₹%.2f). Transparent spending variance check.",
                                    historicalAverageMultiplierThreshold, avgAmount)
                    );
                }
            }
        }

        return SpendingRuleResult.clean();
    }
}
