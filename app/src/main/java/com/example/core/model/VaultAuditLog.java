package com.example.core.model;

import java.io.Serializable;

/**
 * Immutable log record representing an access or transaction event against the Emergency Vault.
 */
public class VaultAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ActionType {
        DEPOSIT,
        WITHDRAWAL,
        UNLOCK_ATTEMPT,
        LOCKOUT_TRIGGERED
    }

    private final long timestamp;
    private final ActionType actionType;
    private final double amount;
    private final boolean isSuccessful;
    private final String reason;
    private final double resultingVaultBalance;

    public VaultAuditLog(long timestamp, ActionType actionType, double amount,
                         boolean isSuccessful, String reason, double resultingVaultBalance) {
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.actionType = actionType;
        this.amount = amount;
        this.isSuccessful = isSuccessful;
        this.reason = reason != null ? reason : "";
        this.resultingVaultBalance = resultingVaultBalance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getReason() {
        return reason;
    }

    public double getResultingVaultBalance() {
        return resultingVaultBalance;
    }

    @Override
    public String toString() {
        return "VaultAuditLog{" +
                "action=" + actionType +
                ", amount=" + amount +
                ", success=" + isSuccessful +
                ", resultingBalance=" + resultingVaultBalance +
                '}';
    }
}
