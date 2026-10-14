package com.upismart.backend.core.payment;

import com.upismart.backend.core.exception.InvalidPaymentDetailsException;
import com.upismart.backend.core.model.PaymentRequest;

/**
 * Concrete processor for scheduled and automated recurring bill payments.
 * Demonstrates Polymorphism and alternate payment strategy execution without interactive PIN entry.
 */
public class ScheduledBillPaymentProcessor extends BasePaymentProcessor {

    public ScheduledBillPaymentProcessor(String systemAuthorizationToken) {
        super(systemAuthorizationToken);
    }

    @Override
    public String getProcessorType() {
        return "SCHEDULED_RECURRING_BILL";
    }

    @Override
    public boolean verifyPin(String enteredPin) {
        // Scheduled recurring payments use pre-authorized system tokens or bypass user PIN prompt
        return true;
    }

    @Override
    protected void performCustomValidation(PaymentRequest request) throws InvalidPaymentDetailsException {
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new InvalidPaymentDetailsException("Billing category (e.g., Rent, Electricity) is mandatory for bills.", "category");
        }
    }
}
