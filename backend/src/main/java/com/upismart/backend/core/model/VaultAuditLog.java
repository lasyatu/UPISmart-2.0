package com.upismart.backend.core.model;

import java.io.Serializable;

/**
 * Immutable log record representing an access or transaction event against the Emergency Vault.
 */
public class VaultAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String logId;
    private final String actionType;
    private final double amount;
    private final boolean isSuccessful;
    private final String failureReason;
    private final double resultingBalance;
    private final long timestamp;

    public VaultAuditLog(String logId, String actionType, double amount, boolean isSuccessful,
                         String failureReason, double resultingBalance, long timestamp) {
        this.logId = logId;
        this.actionType = actionType;
        this.amount = amount;
        this.isSuccessful = isSuccessful;
        this.failureReason = failureReason != null ? failureReason : "";
        this.resultingBalance = resultingBalance;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public String getLogId() {
        return logId;
    }

    public String getActionType() {
        return actionType;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public double getResultingBalance() {
        return resultingBalance;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
