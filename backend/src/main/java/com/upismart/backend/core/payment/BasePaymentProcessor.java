package com.upismart.backend.core.payment;

import com.upismart.backend.core.exception.InsufficientBalanceException;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.exception.InvalidPaymentDetailsException;
import com.upismart.backend.core.exception.InvalidPinException;
import com.upismart.backend.core.exception.UnusualSpendingAlertException;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.PaymentRequest;
import com.upismart.backend.core.model.PaymentResult;
import com.upismart.backend.core.model.Transaction;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Abstract Base Payment Processor demonstrating Inheritance and Template Method pattern.
 * Encapsulates core validation and common execution steps, allowing subclasses
 * to customize specific verification and transaction generation logic via method overriding.
 */
public abstract class BasePaymentProcessor implements PaymentProcessor {

    protected static final Pattern UPI_REGEX = Pattern.compile("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$");

    protected final String expectedDemoPin;
    protected int remainingPinAttempts;

    public BasePaymentProcessor(String expectedDemoPin) {
        this.expectedDemoPin = (expectedDemoPin != null && !expectedDemoPin.trim().isEmpty())
                ? expectedDemoPin.trim() : "1234";
        this.remainingPinAttempts = 3;
    }

    @Override
    public void validateDetails(PaymentRequest request)
            throws InvalidAmountException, InvalidPaymentDetailsException {
        if (request == null) {
            throw new InvalidPaymentDetailsException("Payment request payload cannot be null.", "request");
        }
        if (request.getAmount() <= 0.0) {
            throw new InvalidAmountException("Transfer amount must be strictly greater than zero.", request.getAmount());
        }
        if (Double.isNaN(request.getAmount()) || Double.isInfinite(request.getAmount())) {
            throw new InvalidAmountException("Transfer amount is not a valid finite number.", request.getAmount());
        }
        if (request.getRecipientUpi() == null || request.getRecipientUpi().trim().isEmpty()) {
            throw new InvalidPaymentDetailsException("Recipient UPI ID is required.", "recipientUpi");
        }
        // Subclass-specific custom detail validation
        performCustomValidation(request);
    }

    @Override
    public boolean verifyPin(String enteredPin) throws InvalidPinException {
        if (enteredPin == null || enteredPin.trim().isEmpty()) {
            throw new InvalidPinException("PIN cannot be empty. Please enter your 4-digit demo PIN.", remainingPinAttempts);
        }
        if (!expectedDemoPin.equals(enteredPin.trim())) {
            remainingPinAttempts = Math.max(0, remainingPinAttempts - 1);
            throw new InvalidPinException("Incorrect demo PIN. " + remainingPinAttempts + " attempts remaining.", remainingPinAttempts);
        }
        remainingPinAttempts = 3; // Reset on success
        return true;
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request, AccountBalance balance)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   InvalidPinException,
                   InvalidPaymentDetailsException,
                   UnusualSpendingAlertException {

        // 1. Validate parameters
        validateDetails(request);

        // 2. Validate PIN
        verifyPin(request.getEnteredPin());

        // 3. Balance verification
        double available = balance != null ? balance.getAvailableBalance() : 0.0;
        if (request.getAmount() > available) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient funds: Requested ₹%.2f exceeds unreserved available balance of ₹%.2f.",
                            request.getAmount(), available),
                    request.getAmount(),
                    available
            );
        }

        // 4. Unusual spending rule check (hook method for subclasses)
        checkUnusualSpending(request, balance);

        // 5. Generate and return transaction result
        String refId = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction transaction = createTransaction(refId, request);
        double remainingSafe = Math.max(0.0, available - request.getAmount());

        return PaymentResult.success(
                refId,
                "Simulated payment of ₹" + String.format("%.2f", request.getAmount()) + " to " + request.getRecipientName() + " completed successfully.",
                transaction,
                remainingSafe
        );
    }

    /**
     * Hook method for subclass-specific validation (overridden by subclasses).
     */
    protected abstract void performCustomValidation(PaymentRequest request) throws InvalidPaymentDetailsException;

    /**
     * Hook method for checking unusual spending rules.
     */
    protected void checkUnusualSpending(PaymentRequest request, AccountBalance balance) throws UnusualSpendingAlertException {
        // Default no-op; overridden by DemoUpiPaymentProcessor
    }

    /**
     * Factory method for creating the Transaction model.
     */
    protected Transaction createTransaction(String refId, PaymentRequest request) {
        return new Transaction(
                refId,
                "Paid to " + request.getRecipientName(),
                request.getRecipientUpi(),
                request.getRecipientName(),
                request.getAmount(),
                request.getCategory(),
                System.currentTimeMillis(),
                "SUCCESS",
                request.getNote(),
                true
        );
    }
}
