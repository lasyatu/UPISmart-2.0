package com.example.core.exception;

/**
 * Thrown when a payment or transfer amount is zero, negative, or mathematically invalid.
 */
public class InvalidAmountException extends UpiSmartException {

    private final double attemptedAmount;

    public InvalidAmountException(double attemptedAmount, String message) {
        super("ERR_INVALID_AMOUNT", message);
        this.attemptedAmount = attemptedAmount;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }
}
