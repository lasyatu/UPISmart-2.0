package com.upismart.backend.core.exception;

/**
 * Thrown when an entered security PIN does not match the configured user/vault authentication PIN.
 */
public class InvalidPinException extends UpiSmartException {

    private final int remainingAttempts;

    public InvalidPinException(String message, int remainingAttempts) {
        super(message, "INVALID_PIN");
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
