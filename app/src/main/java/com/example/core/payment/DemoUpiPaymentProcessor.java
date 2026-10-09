package com.example.core.payment;

import com.example.core.detector.UnusualSpendingDetector;
import com.example.core.model.PaymentRequest;
import com.example.core.model.Transaction;

/**
 * Concrete implementation of BasePaymentProcessor demonstrating OOP Inheritance and Method Overriding.
 *
 * Implements simulated UPI payments for UPISmart 2.0.
 * Never connects to live banking gateways or NPCI. All transactions are simulated for personal budgeting.
 */
public class DemoUpiPaymentProcessor extends BasePaymentProcessor {

    public DemoUpiPaymentProcessor() {
        super();
    }

    public DemoUpiPaymentProcessor(UnusualSpendingDetector detector) {
        super(detector);
    }

    @Override
    public String getProcessorType() {
        return "DEMO_UPI_P2P_QR";
    }

    /**
     * Overrides executeTransaction to construct the simulated UPI transaction entity.
     * Demonstrates Polymorphism and Method Overriding.
     */
    @Override
    protected Transaction executeTransaction(PaymentRequest request, String referenceId) {
        String title = request.getRecipientName();
        if (title == null || title.trim().isEmpty()) {
            title = request.getRecipientUpiId().split("@")[0];
        }

        String note = request.getNote();
        if (note == null || note.trim().isEmpty()) {
            note = request.isQrScanned() ? "Simulated QR Scan & Pay" : "Simulated UPI Payment";
        }

        Transaction transaction = new Transaction();
        transaction.setTitle(title);
        transaction.setRecipientUpiId(request.getRecipientUpiId());
        transaction.setRecipientName(request.getRecipientName());
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory());
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setStatus("SUCCESS");
        transaction.setReferenceId(referenceId);
        transaction.setNote(note);
        transaction.setSimulated(true);

        return transaction;
    }
}
