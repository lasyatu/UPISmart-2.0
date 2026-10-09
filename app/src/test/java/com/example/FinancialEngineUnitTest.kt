package com.example

import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AlertSeverity
import com.example.domain.service.FinancialEngine
import com.example.domain.service.QrPaymentParser
import com.example.domain.service.SpendingDetectiveEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FinancialEngineUnitTest {

    @Test
    fun testFinancialCalculations_safeToSpendAndReservations() {
        // Set up fixed calendar: Day 10 of a 30-day month -> 21 remaining days (10 to 30 inclusive)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 10)
        }

        val monthlyBudget = 50000.0

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                title = "Groceries",
                recipientUpiId = "store@demo",
                recipientName = "Store",
                amount = 5000.0,
                category = "Food",
                timestamp = cal.timeInMillis,
                referenceId = "REF1"
            ),
            TransactionEntity(
                id = 2,
                title = "Metro",
                recipientUpiId = "metro@demo",
                recipientName = "Metro",
                amount = 1000.0,
                category = "Travel",
                timestamp = cal.timeInMillis,
                referenceId = "REF2"
            )
        )

        val bills = listOf(
            BillEntity(
                id = 1,
                title = "Electricity",
                category = "Bills",
                amount = 2500.0,
                dueDayOfMonth = 15,
                isPaid = false
            ),
            BillEntity(
                id = 2,
                title = "Water",
                category = "Bills",
                amount = 500.0,
                dueDayOfMonth = 5,
                isPaid = true // Paid bill MUST NOT be reserved
            )
        )

        val recurring = listOf(
            RecurringTransactionEntity(
                id = 1,
                payeeName = "Gym",
                payeeUpiId = "gym@demo",
                amount = 1500.0,
                category = "Health",
                frequency = "MONTHLY",
                nextDueDate = cal.timeInMillis + (5 * 24 * 60 * 60 * 1000L), // due in 5 days
                isActive = true
            )
        )

        val summary = FinancialEngine.calculateSummary(
            monthlyBudget = monthlyBudget,
            transactions = transactions,
            bills = bills,
            recurringList = recurring,
            calendar = cal
        )

        // Total spent: 5000 + 1000 = 6000
        assertEquals(6000.0, summary.totalSpentThisMonth, 0.01)

        // Remaining budget: 50000 - 6000 = 44000
        assertEquals(44000.0, summary.remainingBudget, 0.01)

        // Unpaid bills + recurring: 2500 (unpaid electricity) + 1500 (gym) = 4000
        // (Water at 500 is paid, must NOT be added)
        assertEquals(4000.0, summary.unpaidBillsReserved, 0.01)

        // Unreserved balance: 44000 - 4000 = 40000
        assertEquals(40000.0, summary.unreservedBalance, 0.01)

        // Remaining days: October has 31 days. 31 - 10 + 1 = 22 days
        val remainingDays = FinancialEngine.getRemainingDaysInMonth(cal)
        assertEquals(22, remainingDays)

        // Safe to spend per day: 40000 / 22 = 1818.18
        assertEquals(1818.18, summary.safeToSpendPerDay, 0.02)
    }

    @Test
    fun testSpendingDetective_twentyPercentRule() {
        val budget = 50000.0
        val cal = Calendar.getInstance()

        // 12,000 tx is 24% of budget (>20%)
        val txs = listOf(
            TransactionEntity(
                id = 1,
                title = "Laptop Repair",
                recipientUpiId = "tech@demo",
                recipientName = "Tech Repair",
                amount = 12000.0,
                category = "Shopping",
                timestamp = cal.timeInMillis,
                referenceId = "REF_BIG"
            )
        )

        val alerts = SpendingDetectiveEngine.analyze(budget, txs, cal)
        val highImpactAlert = alerts.firstOrNull { it.id == "high_impact_tx" }

        assertNotNull("Should detect high impact transaction exceeding 20%", highImpactAlert)
        assertTrue(highImpactAlert!!.explanation.contains("20%"))
        assertEquals(AlertSeverity.WARNING, highImpactAlert.severity)
    }

    @Test
    fun testSpendingDetective_eightyPercentThreshold() {
        val budget = 50000.0
        val cal = Calendar.getInstance()

        // Total 42,000 spent out of 50,000 = 84% (>80%)
        val txs = listOf(
            TransactionEntity(
                id = 1,
                title = "Rent Payment",
                recipientUpiId = "landlord@demo",
                recipientName = "Landlord",
                amount = 42000.0,
                category = "Bills",
                timestamp = cal.timeInMillis,
                referenceId = "REF_RENT"
            )
        )

        val alerts = SpendingDetectiveEngine.analyze(budget, txs, cal)
        val budgetAlert = alerts.firstOrNull { it.id == "budget_80_percent" }

        assertNotNull("Should warn when 80% or more of monthly budget is consumed", budgetAlert)
        assertTrue(budgetAlert!!.message.contains("84%"))
    }

    @Test
    fun testQrPaymentParser_validAndInvalid() {
        // Valid standard upi://pay format
        val standardUpiUri = "upi://pay?pa=freshmart@icici&pn=Fresh%20Mart&am=450.50&cu=INR&tn=Daily%20Supplies"
        val parsedStandard = QrPaymentParser.parse(standardUpiUri)
        assertTrue(parsedStandard.isValid)
        assertEquals("freshmart@icici", parsedStandard.recipientUpiId)
        assertEquals("Fresh Mart", parsedStandard.merchantName)
        assertEquals(450.50, parsedStandard.amount!!, 0.01)
        assertEquals("Daily Supplies", parsedStandard.note)

        // Valid demo format
        val validDemoUri = "upismart://pay?recipient=merchant@demo&name=Demo%20Merchant"
        val parsedDemo = QrPaymentParser.parse(validDemoUri)
        assertTrue(parsedDemo.isValid)
        assertEquals("merchant@demo", parsedDemo.recipientUpiId)
        assertEquals("Demo Merchant", parsedDemo.merchantName)

        // Invalid scheme
        val invalidScheme = "https://evil.com/pay?recipient=merchant@demo"
        val parsedInvalidScheme = QrPaymentParser.parse(invalidScheme)
        assertFalse(parsedInvalidScheme.isValid)

        // Invalid recipient structure (no @)
        val invalidRecipient = "upi://pay?pa=invaliduser&pn=Bad%20User"
        val parsedBad = QrPaymentParser.parse(invalidRecipient)
        assertFalse(parsedBad.isValid)

        // Empty input
        val parsedEmpty = QrPaymentParser.parse("")
        assertFalse(parsedEmpty.isValid)
    }
}
