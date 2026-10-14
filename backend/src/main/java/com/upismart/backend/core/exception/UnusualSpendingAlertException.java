package com.upismart.backend.core.exception;

/**
 * Thrown or issued as a checked rule event when an attempted transaction triggers
 * rule-based unusual spending detection (e.g., exceeds 20% of monthly budget or 80% consumption).
 */
public class UnusualSpendingAlertException extends UpiSmartException {

    private final double attemptedAmount;
    private final double thresholdAmount;
    private final String reason;

    public UnusualSpendingAlertException(String message, double attemptedAmount, double thresholdAmount, String reason) {
        super(message, "UNUSUAL_SPENDING_DETECTED");
        this.attemptedAmount = attemptedAmount;
        this.thresholdAmount = thresholdAmount;
        this.reason = reason;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getThresholdAmount() {
        return thresholdAmount;
    }

    public String getReason() {
        return reason;
    }
}
