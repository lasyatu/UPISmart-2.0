package com.example.core.exception;

/**
 * Thrown or issued as a checked rule event when an attempted transaction triggers
 * rule-based unusual spending detection (e.g., exceeds 20% of monthly budget or 80% consumption).
 */
public class UnusualSpendingAlertException extends UpiSmartException {

    private final double attemptedAmount;
    private final double budgetThreshold;
    private final double percentOfBudget;

    public UnusualSpendingAlertException(double attemptedAmount, double budgetThreshold, double percentOfBudget, String message) {
        super("ERR_UNUSUAL_SPENDING", message);
        this.attemptedAmount = attemptedAmount;
        this.budgetThreshold = budgetThreshold;
        this.percentOfBudget = percentOfBudget;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getBudgetThreshold() {
        return budgetThreshold;
    }

    public double getPercentOfBudget() {
        return percentOfBudget;
    }
}
