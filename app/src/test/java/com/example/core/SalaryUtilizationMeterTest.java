package com.example.core;

import com.example.core.budget.SalaryUtilizationMeter;
import com.example.core.model.Transaction;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the 100% Salary-Utilization Meter business logic.
 * Verifies mathematical formulas:
 * Percentage spent = (Total expenditure / Monthly income) * 100
 * Remaining balance = Monthly income - Total expenditure
 */
public class SalaryUtilizationMeterTest {

    @Test
    public void testNormalUtilization() {
        double income = 50000.0;
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, "Rent", "landlord@demo", "Landlord", 15000.0, "Bills", System.currentTimeMillis(), "SUCCESS", "R1", "", true),
                new Transaction(2L, "Groceries", "market@demo", "Supermarket", 5000.0, "Food", System.currentTimeMillis(), "SUCCESS", "R2", "", true)
        );

        SalaryUtilizationMeter meter = SalaryUtilizationMeter.compute(income, transactions);

        assertEquals(50000.0, meter.getMonthlyIncome(), 0.001);
        assertEquals(20000.0, meter.getTotalExpenditure(), 0.001);
        assertEquals(30000.0, meter.getRemainingBalance(), 0.001);
        // (20,000 / 50,000) * 100 = 40.0%
        assertEquals(40.0, meter.getPercentageSpent(), 0.01);
        // 100 - 40 = 60.0%
        assertEquals(60.0, meter.getPercentageRemaining(), 0.01);
        assertFalse(meter.isOverBudget());
        assertEquals(15000.0, meter.getExpensesByCategory().get("Bills"), 0.01);
        assertEquals(5000.0, meter.getExpensesByCategory().get("Food"), 0.01);
    }

    @Test
    public void testHundredPercentUtilization() {
        double income = 40000.0;
        List<Transaction> transactions = Collections.singletonList(
                new Transaction(1L, "Total Outlay", "vendor@demo", "Vendor", 40000.0, "Shopping", System.currentTimeMillis(), "SUCCESS", "R1", "", true)
        );

        SalaryUtilizationMeter meter = SalaryUtilizationMeter.compute(income, transactions);

        assertEquals(40000.0, meter.getTotalExpenditure(), 0.001);
        assertEquals(0.0, meter.getRemainingBalance(), 0.001);
        assertEquals(100.0, meter.getPercentageSpent(), 0.01);
        assertEquals(0.0, meter.getPercentageRemaining(), 0.01);
        assertFalse(meter.isOverBudget());
    }

    @Test
    public void testOverBudgetSpending() {
        double income = 30000.0;
        List<Transaction> transactions = Collections.singletonList(
                new Transaction(1L, "Major Emergency Debit", "hospital@demo", "Hospital", 45000.0, "Health", System.currentTimeMillis(), "SUCCESS", "R1", "", true)
        );

        SalaryUtilizationMeter meter = SalaryUtilizationMeter.compute(income, transactions);

        assertEquals(45000.0, meter.getTotalExpenditure(), 0.001);
        // Remaining balance = 30,000 - 45,000 = -15,000
        assertEquals(-15000.0, meter.getRemainingBalance(), 0.001);
        // (45,000 / 30,000) * 100 = 150.0%
        assertEquals(150.0, meter.getPercentageSpent(), 0.01);
        assertEquals(0.0, meter.getPercentageRemaining(), 0.01);
        assertTrue("Must flag over-budget spending safely", meter.isOverBudget());
    }

    @Test
    public void testZeroIncomeEdgeCase() {
        double income = 0.0;
        List<Transaction> transactions = Collections.singletonList(
                new Transaction(1L, "Expense with zero salary", "payee@demo", "Payee", 200.0, "Other", System.currentTimeMillis(), "SUCCESS", "R1", "", true)
        );

        // Must not crash with ArithmeticException divide-by-zero
        SalaryUtilizationMeter meter = SalaryUtilizationMeter.compute(income, transactions);

        assertEquals(0.0, meter.getMonthlyIncome(), 0.001);
        assertEquals(200.0, meter.getTotalExpenditure(), 0.001);
        assertEquals(-200.0, meter.getRemainingBalance(), 0.001);
        assertTrue(meter.isOverBudget());
    }

    @Test
    public void testIgnoresFailedTransactions() {
        double income = 50000.0;
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, "Failed Payment", "fail@demo", "Fail", 10000.0, "Food", System.currentTimeMillis(), "FAILED", "R1", "", true),
                new Transaction(2L, "Successful Payment", "success@demo", "Success", 5000.0, "Food", System.currentTimeMillis(), "SUCCESS", "R2", "", true)
        );

        SalaryUtilizationMeter meter = SalaryUtilizationMeter.compute(income, transactions);
        assertEquals(5000.0, meter.getTotalExpenditure(), 0.001);
        assertEquals(45000.0, meter.getRemainingBalance(), 0.001);
    }
}
