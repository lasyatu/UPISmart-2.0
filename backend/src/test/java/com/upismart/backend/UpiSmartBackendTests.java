package com.upismart.backend;

import com.upismart.backend.core.budget.BudgetManager;
import com.upismart.backend.core.budget.SalaryUtilizationMeter;
import com.upismart.backend.core.detector.SpendingRuleResult;
import com.upismart.backend.core.detector.UnusualSpendingDetector;
import com.upismart.backend.core.exception.*;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.PaymentRequest;
import com.upismart.backend.core.model.PaymentResult;
import com.upismart.backend.core.model.Transaction;
import com.upismart.backend.core.payment.DemoUpiPaymentProcessor;
import com.upismart.backend.core.vault.EmergencyVault;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UpiSmartBackendTests {

    @Test
    public void testSalaryUtilizationMeterFormulas() {
        double monthlySalary = 50000.0;
        Transaction tx1 = new Transaction("T1", "Groceries", "shop@upi", "Shop", 5000.0, "Groceries", System.currentTimeMillis(), "SUCCESS", "", true);
        Transaction tx2 = new Transaction("T2", "Electricity", "power@upi", "Power", 2500.0, "Bills", System.currentTimeMillis(), "SUCCESS", "", true);

        SalaryUtilizationMeter meter = SalaryUtilizationMeter.calculate(monthlySalary, List.of(tx1, tx2), 0.0);

        assertEquals(50000.0, meter.getMonthlyIncome());
        assertEquals(7500.0, meter.getTotalExpenditure());
        assertEquals(42500.0, meter.getRemainingBalance());
        assertEquals(15.0, meter.getPercentageSpent(), 0.01);
        assertEquals(85.0, meter.getPercentageRemaining(), 0.01);
        assertEquals("OPTIMAL", meter.getUtilizationStatus());
    }

    @Test
    public void testDemoUpiPaymentProcessor_ValidTransaction() throws Exception {
        DemoUpiPaymentProcessor processor = new DemoUpiPaymentProcessor("1234", new UnusualSpendingDetector());
        AccountBalance balance = new AccountBalance(50000.0, 10000.0, 0.0, 15000.0); // available = 25000

        PaymentRequest request = new PaymentRequest("merchant@upi", "Merchant", 1500.0, "Shopping", "1234", "Shoes", false);
        PaymentResult result = processor.processPayment(request, balance);

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransaction());
        assertEquals(1500.0, result.getTransaction().getAmount());
        assertEquals(23500.0, result.getRemainingSafeToSpend());
    }

    @Test
    public void testDemoUpiPaymentProcessor_InsufficientFunds() {
        DemoUpiPaymentProcessor processor = new DemoUpiPaymentProcessor("1234", new UnusualSpendingDetector());
        AccountBalance balance = new AccountBalance(50000.0, 40000.0, 0.0, 9000.0); // available = 1000

        PaymentRequest request = new PaymentRequest("merchant@upi", "Merchant", 2500.0, "Shopping", "1234", "Electronics", false);

        assertThrows(InsufficientBalanceException.class, () -> {
            processor.processPayment(request, balance);
        });
    }

    @Test
    public void testDemoUpiPaymentProcessor_InvalidPin() {
        DemoUpiPaymentProcessor processor = new DemoUpiPaymentProcessor("1234", new UnusualSpendingDetector());
        AccountBalance balance = new AccountBalance(50000.0, 10000.0, 0.0, 0.0);

        PaymentRequest request = new PaymentRequest("friend@upi", "Friend", 500.0, "Food", "9999", "Lunch", false);

        assertThrows(InvalidPinException.class, () -> {
            processor.processPayment(request, balance);
        });
    }

    @Test
    public void testEmergencyVault_LockoutProtection() throws Exception {
        EmergencyVault vault = new EmergencyVault(15000.0, "1234");

        // 3 failed attempts
        assertThrows(InvalidPinException.class, () -> vault.unlock("0000"));
        assertThrows(InvalidPinException.class, () -> vault.unlock("1111"));
        assertThrows(VaultLockedException.class, () -> vault.unlock("2222"));

        // Subsequent access blocked by lockout
        assertThrows(VaultLockedException.class, () -> vault.unlock("1234"));
    }

    @Test
    public void testUnusualSpendingDetector_RuleTrigger() {
        UnusualSpendingDetector detector = new UnusualSpendingDetector(0.20, 15000.0);

        // 20% of 50,000 = 10,000. Attempt 12,000 -> should trigger alert
        SpendingRuleResult result = detector.evaluate(12000.0, 50000.0, null);
        assertTrue(result.isUnusual());
        assertEquals("SALARY_THRESHOLD_EXCEEDED", result.getReason());
    }
}
