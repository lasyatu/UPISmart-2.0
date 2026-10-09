package com.example.core.payment;

import com.example.core.exception.InsufficientBalanceException;
import com.example.core.exception.InvalidAmountException;
import com.example.core.exception.InvalidPaymentDetailsException;
import com.example.core.exception.InvalidPinException;
import com.example.core.exception.UpiSmartException;
import com.example.core.model.PaymentRequest;
import com.example.core.model.PaymentResult;

/**
 * Common payment processor interface establishing polymorphism across payment processing strategies.
 * Implementations may handle simulated UPI QR payments, scheduled bills, or manual transfers.
 */
public interface PaymentProcessor {

    /**
     * Validates payment request parameters without committing or mutating account state.
     * Throws custom exceptions if validation fails.
     */
    void validateRequest(PaymentRequest request, double availableBalance)
            throws InvalidAmountException, InvalidPaymentDetailsException, InsufficientBalanceException;

    /**
     * Executes the payment workflow.
     * Demonstrates polymorphic execution based on specific processor strategies.
     */
    PaymentResult processPayment(PaymentRequest request, double availableBalance, String userPin)
            throws UpiSmartException;

    /**
     * Identifies the processor type.
     */
    String getProcessorType();
}
