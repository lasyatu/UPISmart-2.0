package com.upismart.backend.core.payment;

import com.upismart.backend.core.detector.SpendingRuleResult;
import com.upismart.backend.core.detector.UnusualSpendingDetector;
import com.upismart.backend.core.exception.InvalidPaymentDetailsException;
import com.upismart.backend.core.exception.UnusualSpendingAlertException;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.PaymentRequest;

/**
 * Concrete implementation of the Simulated UPI Payment Engine.
 * Demonstrates Inheritance (extends BasePaymentProcessor) and Method Overriding.
 */
public class DemoUpiPaymentProcessor extends BasePaymentProcessor {

    private final UnusualSpendingDetector detector;

    public DemoUpiPaymentProcessor(String expectedDemoPin, UnusualSpendingDetector detector) {
        super(expectedDemoPin);
        this.detector = detector != null ? detector : new UnusualSpendingDetector();
    }

    @Override
    public String getProcessorType() {
        return "DEMO_UPI_P2P";
    }

    @Override
    protected void performCustomValidation(PaymentRequest request) throws InvalidPaymentDetailsException {
        String upi = request.getRecipientUpi();
        // Accepts standard vpa@bank or demo tags
        if (!UPI_REGEX.matcher(upi).matches() && !upi.contains("@")) {
            throw new InvalidPaymentDetailsException(
                    "Invalid UPI VPA format '" + upi + "'. Expected format: username@bank (e.g., alex@upi).",
                    "recipientUpi"
            );
        }
    }

    @Override
    protected void checkUnusualSpending(PaymentRequest request, AccountBalance balance)
            throws UnusualSpendingAlertException {
        if (request.isUserConfirmed()) {
            // User explicitly confirmed after being alerted
            return;
        }

        if (balance != null && detector != null) {
            SpendingRuleResult ruleResult = detector.evaluate(request.getAmount(), balance.getMonthlySalary(), null);
            if (ruleResult.isUnusual()) {
                throw new UnusualSpendingAlertException(
                        ruleResult.getAlertMessage(),
                        request.getAmount(),
                        ruleResult.getThresholdTriggered(),
                        ruleResult.getReason()
                );
            }
        }
    }
}
