package com.example.core.exception;

/**
 * Base exception for the UPISmart core financial processing engine.
 */
public class UpiSmartException extends Exception {

    private final String errorCode;

    public UpiSmartException(String message) {
        super(message);
        this.errorCode = "ERR_GENERAL";
    }

    public UpiSmartException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UpiSmartException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "UpiSmartException[" + errorCode + "]: " + getMessage();
    }
}
