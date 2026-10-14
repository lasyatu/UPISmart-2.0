package com.upismart.backend.core.exception;

/**
 * Base checked exception for the UPISmart core financial processing engine.
 * Demonstrates OOP exception hierarchy and encapsulation of error details.
 */
public class UpiSmartException extends Exception {

    private final String errorCode;

    public UpiSmartException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UpiSmartException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
