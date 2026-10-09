package com.example.core;

import com.example.core.exception.InsufficientBalanceException;
import com.example.core.exception.InvalidAmountException;
import com.example.core.exception.InvalidPaymentDetailsException;
import com.example.core.exception.InvalidPinException;
import com.example.core.exception.UpiSmartException;
import com.example.core.model.PaymentRequest;
import com.example.core.model.PaymentResult;
import com.example.core.model.Transaction;
import com.example.core.payment.DemoUpiPaymentProcessor;
import com.example.core.payment.PaymentProcessor;
import com.example.core.payment.ScheduledBillPaymentProcessor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests verifying OOP payment processing, inheritance, polymorphism,
 * and custom exception throwing.
 */
public class PaymentProcessorTest {

    private PaymentProcessor upiProcessor;
    private PaymentProcessor billProcessor;

    @Before
    public void setUp() {
        // Polymorphic instantiation via common interface
        upiProcessor = new DemoUpiPaymentProcessor();
        billProcessor = new ScheduledBillPaymentProcessor();
    }

    @Test
    public void testValidSimulatedPayment() throws UpiSmartException {
        PaymentRequest request = new PaymentRequest(
                "freshmart@demo",
                "Fresh Mart",
                450.0,
                "Food",
                "Weekly organic groceries",
                "1234"
        );

        double availableBalance = 10000.0;
        PaymentResult result = upiProcessor.processPayment(request, availableBalance, "1234");

        assertTrue("Payment must succeed", result.isSuccessful());
        assertEquals(PaymentResult.Status.SUCCESS, result.getStatus());
        assertEquals(9550.0, result.getUpdatedBalance(), 0.001);
        assertNotNull("Must return generated transaction", result.getTransaction());

        Transaction tx = result.getTransaction();
        assertEquals("Fresh Mart", tx.getRecipientName());
        assertEquals("freshmart@demo", tx.getRecipientUpiId());
        assertEquals(450.0, tx.getAmount(), 0.001);
        assertEquals("SUCCESS", tx.getStatus());
        assertTrue(tx.getReferenceId().startsWith("UPI/DEMO/2026/"));
    }

    @Test
    public void testNegativeAmountThrowsInvalidAmountException() {
        PaymentRequest request = new PaymentRequest(
                "store@demo", "Store", -50.0, "Shopping", "", "1234"
        );

        try {
            upiProcessor.processPayment(request, 5000.0, "1234");
            fail("Must throw InvalidAmountException for negative amount");
        } catch (InvalidAmountException e) {
            assertEquals(-50.0, e.getAttemptedAmount(), 0.001);
            assertEquals("ERR_INVALID_AMOUNT", e.getErrorCode());
        } catch (UpiSmartException e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    @Test
    public void testZeroAmountThrowsInvalidAmountException() {
        PaymentRequest request = new PaymentRequest(
                "store@demo", "Store", 0.0, "Shopping", "", "1234"
        );

        try {
            upiProcessor.processPayment(request, 5000.0, "1234");
            fail("Must throw InvalidAmountException for zero amount");
        } catch (InvalidAmountException e) {
            assertEquals(0.0, e.getAttemptedAmount(), 0.001);
        } catch (UpiSmartException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void testInvalidUpiFormatThrowsInvalidPaymentDetailsException() {
        // Missing '@' handle delimiter
        PaymentRequest request = new PaymentRequest(
                "invalidhandlewithoutat", "Bad Payee", 200.0, "Food", "", "1234"
        );

        try {
            upiProcessor.processPayment(request, 5000.0, "1234");
            fail("Must throw InvalidPaymentDetailsException for malformed UPI ID");
        } catch (InvalidPaymentDetailsException e) {
            assertEquals("recipientUpiId", e.getInvalidField());
            assertEquals("ERR_INVALID_PAYMENT_DETAILS", e.getErrorCode());
        } catch (UpiSmartException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void testInsufficientFundsThrowsException() {
        PaymentRequest request = new PaymentRequest(
                "landlord@demo", "Landlord", 25000.0, "Bills", "Rent", "1234"
        );

        double balance = 15000.0; // Less than requested 25,000
        try {
            upiProcessor.processPayment(request, balance, "1234");
            fail("Must throw InsufficientBalanceException");
        } catch (InsufficientBalanceException e) {
            assertEquals(25000.0, e.getAttemptedAmount(), 0.001);
            assertEquals(15000.0, e.getAvailableBalance(), 0.001);
            assertEquals("ERR_INSUFFICIENT_FUNDS", e.getErrorCode());
        } catch (UpiSmartException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void testInvalidPinThrowsInvalidPinException() {
        PaymentRequest request = new PaymentRequest(
                "friend@demo", "Friend", 500.0, "Food", "Dinner split", "9999" // Wrong PIN
        );

        try {
            upiProcessor.processPayment(request, 10000.0, "1234");
            fail("Must throw InvalidPinException for incorrect PIN");
        } catch (InvalidPinException e) {
            assertEquals("ERR_INVALID_PIN", e.getErrorCode());
            assertEquals(2, e.getRemainingAttempts());
        } catch (UpiSmartException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void testPolymorphicExecution_scheduledBillProcessor() throws UpiSmartException {
        // ScheduledBillPaymentProcessor overrides validatePin to allow automated mandate execution
        PaymentRequest request = new PaymentRequest(
                "electricity@demo", "Power Corp", 2400.0, "Bills", "Monthly bill", ""
        );

        PaymentResult result = billProcessor.processPayment(request, 10000.0, "1234");
        assertTrue(result.isSuccessful());
        assertEquals("SCHEDULED_BILL_AUTOPAY", billProcessor.getProcessorType());
        assertEquals("Bills", result.getTransaction().getCategory());
    }
}
