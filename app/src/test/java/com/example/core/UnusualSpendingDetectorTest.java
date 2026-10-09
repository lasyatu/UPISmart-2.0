package com.example.core;

import com.example.core.detector.SpendingRuleResult;
import com.example.core.detector.UnusualSpendingDetector;
import com.example.core.model.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying transparent, rule-based unusual spending detection.
 * Strictly rule-based heuristics — not machine learning.
 */
public class UnusualSpendingDetectorTest {

    private UnusualSpendingDetector detector;

    @Before
    public void setUp() {
        detector = new UnusualSpendingDetector(0.20, 3.0);
    }

    @Test
    public void testCleanNormalTransaction() {
        double monthlyBudget = 50000.0;
        double remaining = 40000.0;
        double amount = 500.0; // 1% of budget, very low

        SpendingRuleResult result = detector.evaluatePrePayment(amount, monthlyBudget, remaining, Collections.emptyList());
        assertFalse("Normal small payment should not be flagged", result.isFlagged());
        assertEquals(SpendingRuleResult.RuleType.NORMAL, result.getRuleType());
    }

    @Test
    public void testHighBudgetPercentageRule() {
        double monthlyBudget = 50000.0;
        double remaining = 30000.0;
        double amount = 12000.0; // 24% of budget (>20% threshold)

        SpendingRuleResult result = detector.evaluatePrePayment(amount, monthlyBudget, remaining, Collections.emptyList());
        assertTrue("Payment exceeding 20% of monthly budget must be flagged", result.isFlagged());
        assertEquals(SpendingRuleResult.RuleType.HIGH_BUDGET_PERCENTAGE, result.getRuleType());
        assertTrue(result.getTitle().contains("High Impact"));
        assertTrue(result.getMessage().contains("24.0%"));
        assertTrue(result.getExplanation().contains("Rule: proposedAmount >= 20%"));
    }

    @Test
    public void testExceedsRemainingBudgetRule() {
        double monthlyBudget = 50000.0;
        double remaining = 5000.0; // only 5,000 left
        double amount = 7000.0; // Exceeds remaining

        SpendingRuleResult result = detector.evaluatePrePayment(amount, monthlyBudget, remaining, Collections.emptyList());
        assertTrue("Payment exceeding remaining balance must be flagged", result.isFlagged());
        assertEquals(SpendingRuleResult.RuleType.EXCEEDS_REMAINING_BUDGET, result.getRuleType());
        assertTrue(result.getMessage().contains("exceeds your remaining"));
    }

    @Test
    public void testMultiplierOfHistoricalAverageRule() {
        double monthlyBudget = 100000.0;
        double remaining = 80000.0;

        // Average of past debits = (300 + 400 + 500) / 3 = 400
        List<Transaction> history = Arrays.asList(
                new Transaction(1L, "Coffee", "c@demo", "Cafe", 300.0, "Food", System.currentTimeMillis(), "SUCCESS", "R1", "", true),
                new Transaction(2L, "Lunch", "l@demo", "Diner", 400.0, "Food", System.currentTimeMillis(), "SUCCESS", "R2", "", true),
                new Transaction(3L, "Snacks", "s@demo", "Bakery", 500.0, "Food", System.currentTimeMillis(), "SUCCESS", "R3", "", true)
        );

        // 2,000 is 5x the average of 400 (exceeds 3.0x multiplier threshold)
        double amount = 2000.0;

        SpendingRuleResult result = detector.evaluatePrePayment(amount, monthlyBudget, remaining, history);
        assertTrue("Payment >= 3x average must be flagged as unusual spending", result.isFlagged());
        assertEquals(SpendingRuleResult.RuleType.MULTIPLIER_OF_AVERAGE, result.getRuleType());
        assertTrue(result.getMessage().contains("5.0x higher than your average"));
    }
}
