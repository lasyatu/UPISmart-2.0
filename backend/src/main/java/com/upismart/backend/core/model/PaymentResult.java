package com.upismart.backend.core.model;

import java.io.Serializable;

/**
 * Encapsulates the immutable outcome of a simulated payment execution.
 */
public class PaymentResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String referenceId;
    private final String message;
    private final Transaction transaction;
    private final double remainingSafeToSpend;

    public PaymentResult(boolean success, String referenceId, String message,
                         Transaction transaction, double remainingSafeToSpend) {
        this.success = success;
        this.referenceId = referenceId != null ? referenceId : "";
        this.message = message != null ? message : "";
        this.transaction = transaction;
        this.remainingSafeToSpend = remainingSafeToSpend;
    }

    public static PaymentResult success(String refId, String message, Transaction tx, double remaining) {
        return new PaymentResult(true, refId, message, tx, remaining);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, "", message, null, 0.0);
    }

    public boolean isSuccess() {
        return success;
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

    public double getRemainingSafeToSpend() {
        return remainingSafeToSpend;
    }
}
