package com.upismart.backend.core.exception;

/**
 * Thrown when a payment or transfer amount is zero, negative, or mathematically invalid.
 */
public class InvalidAmountException extends UpiSmartException {

    private final double attemptedAmount;

    public InvalidAmountException(String message, double attemptedAmount) {
        super(message, "INVALID_AMOUNT");
        this.attemptedAmount = attemptedAmount;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }
}
