package com.example.core.payment;

import com.example.core.exception.InvalidPinException;
import com.example.core.model.PaymentRequest;
import com.example.core.model.Transaction;

/**
 * Concrete implementation for auto-scheduled bill payments.
 * Demonstrates Polymorphism and Method Overriding:
 * - Overrides validatePin to allow automated recurring deductions without interactive PIN prompts.
 * - Categorizes transactions specifically under "Bills".
 */
public class ScheduledBillPaymentProcessor extends BasePaymentProcessor {

    public ScheduledBillPaymentProcessor() {
        super();
    }

    @Override
    public String getProcessorType() {
        return "SCHEDULED_BILL_AUTOPAY";
    }

    /**
     * Overridden hook: Scheduled recurring bills use mandate pre-authorization,
     * so manual PIN is not required for background debit execution.
     */
    @Override
    protected void validatePin(String enteredPin, String configuredPin) throws InvalidPinException {
        // Pre-authorized mandate; skips manual entry validation
    }

    @Override
    protected Transaction executeTransaction(PaymentRequest request, String referenceId) {
        String title = request.getRecipientName() != null ? request.getRecipientName() : "Scheduled Bill";

        Transaction transaction = new Transaction();
        transaction.setTitle(title);
        transaction.setRecipientUpiId(request.getRecipientUpiId());
        transaction.setRecipientName(title);
        transaction.setAmount(request.getAmount());
        transaction.setCategory("Bills");
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setStatus("SUCCESS");
        transaction.setReferenceId(referenceId);
        transaction.setNote("Autopaid Bill: " + (request.getNote().isEmpty() ? title : request.getNote()));
        transaction.setSimulated(true);

        return transaction;
    }
}
