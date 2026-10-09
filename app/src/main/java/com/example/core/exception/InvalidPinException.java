package com.example.core.exception;

/**
 * Thrown when an entered security PIN does not match the configured user/vault authentication PIN.
 */
public class InvalidPinException extends UpiSmartException {

    private final int remainingAttempts;

    public InvalidPinException(String message, int remainingAttempts) {
        super("ERR_INVALID_PIN", message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
