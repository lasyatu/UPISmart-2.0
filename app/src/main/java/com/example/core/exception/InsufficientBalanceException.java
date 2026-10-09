package com.example.core.exception;

/**
 * Thrown when the user's available unreserved balance is less than the requested transaction amount.
 */
public class InsufficientBalanceException extends UpiSmartException {

    private final double attemptedAmount;
    private final double availableBalance;

    public InsufficientBalanceException(double attemptedAmount, double availableBalance, String message) {
        super("ERR_INSUFFICIENT_FUNDS", message);
        this.attemptedAmount = attemptedAmount;
        this.availableBalance = availableBalance;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }
}
