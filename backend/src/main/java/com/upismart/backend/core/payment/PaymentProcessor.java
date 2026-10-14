package com.upismart.backend.core.payment;

import com.upismart.backend.core.exception.InsufficientBalanceException;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.exception.InvalidPaymentDetailsException;
import com.upismart.backend.core.exception.InvalidPinException;
import com.upismart.backend.core.exception.UnusualSpendingAlertException;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.PaymentRequest;
import com.upismart.backend.core.model.PaymentResult;

/**
 * Common payment-processing interface demonstrating Polymorphism.
 * Allows client code to execute different payment strategies (UPI peer-to-peer,
 * scheduled utility bills, merchant payments) transparently.
 */
public interface PaymentProcessor {

    /**
     * Executes the payment workflow: input validation, security authentication,
     * available balance check, rule-based anomaly detection, and state update.
     */
    PaymentResult processPayment(PaymentRequest request, AccountBalance balance)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   InvalidPinException,
                   InvalidPaymentDetailsException,
                   UnusualSpendingAlertException;

    /**
     * Validates payment details prior to confirmation.
     */
    void validateDetails(PaymentRequest request) throws InvalidAmountException, InvalidPaymentDetailsException;

    /**
     * Authenticates the user's PIN.
     */
    boolean verifyPin(String enteredPin) throws InvalidPinException;

    /**
     * Returns the processor type identifier.
     */
    String getProcessorType();
}
