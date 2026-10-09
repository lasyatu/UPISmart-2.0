package com.example.core.model;

import java.io.Serializable;

/**
 * Encapsulates the immutable outcome of a simulated payment execution.
 */
public class PaymentResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        SUCCESS,
        FAILED,
        WARNED
    }

    private final Status status;
    private final String referenceId;
    private final String message;
    private final Transaction transaction;
    private final double updatedBalance;
    private final String warningAlert;

    public PaymentResult(Status status, String referenceId, String message,
                         Transaction transaction, double updatedBalance, String warningAlert) {
        this.status = status;
        this.referenceId = referenceId;
        this.message = message;
        this.transaction = transaction;
        this.updatedBalance = updatedBalance;
        this.warningAlert = warningAlert;
    }

    public static PaymentResult success(String referenceId, Transaction transaction, double updatedBalance) {
        return new PaymentResult(Status.SUCCESS, referenceId, "Payment simulated successfully", transaction, updatedBalance, null);
    }

    public static PaymentResult successWithWarning(String referenceId, Transaction transaction, double updatedBalance, String warning) {
        return new PaymentResult(Status.WARNED, referenceId, "Payment completed with warning", transaction, updatedBalance, warning);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(Status.FAILED, null, message, null, 0.0, null);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS || status == Status.WARNED;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getMessage() {
        return message;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public double getUpdatedBalance() {
        return updatedBalance;
    }

    public String getWarningAlert() {
        return warningAlert;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "status=" + status +
                ", ref='" + referenceId + '\'' +
                ", msg='" + message + '\'' +
                ", updatedBalance=" + updatedBalance +
                '}';
    }
}
