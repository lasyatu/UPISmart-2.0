package com.example.core.exception;

/**
 * Thrown when recipient information, UPI ID format, or payment metadata fails validation.
 */
public class InvalidPaymentDetailsException extends UpiSmartException {

    private final String invalidField;

    public InvalidPaymentDetailsException(String invalidField, String message) {
        super("ERR_INVALID_PAYMENT_DETAILS", message);
        this.invalidField = invalidField;
    }

    public String getInvalidField() {
        return invalidField;
    }
}
