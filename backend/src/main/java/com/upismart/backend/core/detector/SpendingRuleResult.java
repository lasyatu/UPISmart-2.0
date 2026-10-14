package com.upismart.backend.core.detector;

import java.io.Serializable;

/**
 * Result of a transparent rule-based spending check.
 * Strictly deterministic rule-based heuristics — not machine learning.
 */
public class SpendingRuleResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean isUnusual;
    private final String reason;
    private final String alertMessage;
    private final double thresholdTriggered;

    public SpendingRuleResult(boolean isUnusual, String reason, String alertMessage, double thresholdTriggered) {
        this.isUnusual = isUnusual;
        this.reason = reason != null ? reason : "";
        this.alertMessage = alertMessage != null ? alertMessage : "";
        this.thresholdTriggered = thresholdTriggered;
    }

    public static SpendingRuleResult normal() {
        return new SpendingRuleResult(false, "NORMAL", "Spending is within normal parameters.", 0.0);
    }

    public static SpendingRuleResult alert(String reason, String message, double threshold) {
        return new SpendingRuleResult(true, reason, message, threshold);
    }

    public boolean isUnusual() {
        return isUnusual;
    }

    public String getReason() {
        return reason;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public double getThresholdTriggered() {
        return thresholdTriggered;
    }
}
