package com.upismart.backend.core.exception;

/**
 * Thrown when recipient information, UPI ID format, or payment metadata fails validation.
 */
public class InvalidPaymentDetailsException extends UpiSmartException {

    private final String invalidField;

    public InvalidPaymentDetailsException(String message, String invalidField) {
        super(message, "INVALID_PAYMENT_DETAILS");
        this.invalidField = invalidField;
    }

    public String getInvalidField() {
        return invalidField;
    }
}
