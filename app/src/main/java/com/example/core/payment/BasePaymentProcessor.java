package com.example.core.payment;

import com.example.core.detector.SpendingRuleResult;
import com.example.core.detector.UnusualSpendingDetector;
import com.example.core.exception.InsufficientBalanceException;
import com.example.core.exception.InvalidAmountException;
import com.example.core.exception.InvalidPaymentDetailsException;
import com.example.core.exception.InvalidPinException;
import com.example.core.exception.UpiSmartException;
import com.example.core.model.PaymentRequest;
import com.example.core.model.PaymentResult;
import com.example.core.model.Transaction;

import java.util.Random;

/**
 * Abstract base class demonstrating OOP Inheritance and Encapsulation.
 *
 * Implements the Template Method pattern: defines invariant validation routines,
 * and delegates execution steps to concrete subclasses through protected abstract hooks.
 */
public abstract class BasePaymentProcessor implements PaymentProcessor {

    protected final UnusualSpendingDetector unusualSpendingDetector;
    protected final Random random = new Random();

    public BasePaymentProcessor() {
        this.unusualSpendingDetector = new UnusualSpendingDetector();
    }

    public BasePaymentProcessor(UnusualSpendingDetector unusualSpendingDetector) {
        this.unusualSpendingDetector = unusualSpendingDetector != null ?
                unusualSpendingDetector : new UnusualSpendingDetector();
    }

    /**
     * Invariant template method implementation of request validation.
     * Enforces common constraints across all payment processors.
     */
    @Override
    public void validateRequest(PaymentRequest request, double availableBalance)
            throws InvalidAmountException, InvalidPaymentDetailsException, InsufficientBalanceException {
        if (request == null) {
            throw new InvalidPaymentDetailsException("request", "Payment request cannot be null.");
        }

        // Amount must be strictly greater than 0
        if (request.getAmount() <= 0.0) {
            throw new InvalidAmountException(request.getAmount(),
                    "Payment amount must be greater than zero. Entered: ₹" + request.getAmount());
        }

        // Recipient UPI ID validation
        String recipient = request.getRecipientUpiId();
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new InvalidPaymentDetailsException("recipientUpiId", "Recipient UPI ID cannot be empty.");
        }

        if (!isValidUpiFormat(recipient)) {
            throw new InvalidPaymentDetailsException("recipientUpiId",
                    "Invalid UPI ID structure: '" + recipient + "'. Format must be 'username@handle'.");
        }

        // Check available balance
        if (request.getAmount() > availableBalance) {
            throw new InsufficientBalanceException(request.getAmount(), availableBalance,
                    String.format("Insufficient funds. Attempted to pay ₹%.2f, but available balance is ₹%.2f.",
                            request.getAmount(), availableBalance));
        }
    }

    /**
     * Template method implementing the core payment execution pipeline.
     */
    @Override
    public PaymentResult processPayment(PaymentRequest request, double availableBalance, String configuredPin)
            throws UpiSmartException {
        // Step 1 & 4: Validate inputs and check available funds
        validateRequest(request, availableBalance);

        // Step 3: Validate PIN (hook method overridden by subclasses)
        validatePin(request.getDemoPin(), configuredPin);

        // Step 5: Check unusual spending rules
        SpendingRuleResult ruleResult = unusualSpendingDetector.evaluatePrePayment(
                request.getAmount(),
                availableBalance,
                availableBalance,
                null
        );

        // Step 8: Execute the transaction via concrete subclass hook
        String refId = generateReferenceId();
        Transaction transaction = executeTransaction(request, refId);

        // Step 9: Return outcome with updated balance
        double updatedBalance = availableBalance - request.getAmount();

        if (ruleResult.isFlagged()) {
            return PaymentResult.successWithWarning(
                    refId,
                    transaction,
                    updatedBalance,
                    ruleResult.getTitle() + ": " + ruleResult.getMessage()
            );
        } else {
            return PaymentResult.success(refId, transaction, updatedBalance);
        }
    }

    /**
     * Hook method for PIN validation. Subclasses can override if PIN rules differ.
     */
    protected void validatePin(String enteredPin, String configuredPin) throws InvalidPinException {
        if (enteredPin == null || enteredPin.trim().isEmpty()) {
            throw new InvalidPinException("Security PIN cannot be blank.", 3);
        }
        // Accepts configured PIN or default demo PIN "1234"
        boolean match = enteredPin.equals(configuredPin) || "1234".equals(enteredPin);
        if (!match) {
            throw new InvalidPinException("Incorrect demo PIN. Please enter the valid 4-digit PIN.", 2);
        }
    }

    /**
     * Abstract hook method to be implemented by specific concrete subclasses.
     * Demonstrates Polymorphism and Method Overriding.
     */
    protected abstract Transaction executeTransaction(PaymentRequest request, String referenceId);

    /**
     * Utility method checking basic UPI handle format.
     */
    protected boolean isValidUpiFormat(String upiId) {
        if (upiId == null) return false;
        String trimmed = upiId.trim();
        int atIndex = trimmed.indexOf('@');
        return atIndex > 0 && atIndex < trimmed.length() - 1 && trimmed.indexOf('@', atIndex + 1) == -1;
    }

    /**
     * Generates a unique UPI reference number.
     */
    protected String generateReferenceId() {
        int randomDigits = 100000 + random.nextInt(900000);
        return "UPI/DEMO/2026/" + randomDigits;
    }
}
