package com.example.core

import com.example.core.budget.BudgetManager
import com.example.core.budget.SalaryUtilizationMeter
import com.example.core.exception.InsufficientBalanceException
import com.example.core.exception.InvalidAmountException
import com.example.core.exception.InvalidPinException
import com.example.core.model.PaymentRequest
import com.example.core.payment.DemoUpiPaymentProcessor
import com.example.core.vault.EmergencyVault
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.service.FinancialEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Calendar

/**
 * Verifies seamless bidirectional Kotlin-Java interoperability.
 * Asserts that Kotlin code safely consumes Core Java OOP classes, exceptions, and engines.
 */
class JavaKotlinInteroperabilityTest {

    @Test
    fun testKotlinInteroperabilityWithJavaSalaryUtilizationMeter() {
        val income = 60000.0
        val txs = listOf(
            com.example.core.model.Transaction(
                1L, "Rent", "landlord@demo", "Landlord", 20000.0, "Bills", System.currentTimeMillis(), "SUCCESS", "REF1", "", true
            ),
            com.example.core.model.Transaction(
                2L, "Dining", "bistro@demo", "Bistro", 4000.0, "Food", System.currentTimeMillis(), "SUCCESS", "REF2", "", true
            )
        )

        // Invoke Java static method from Kotlin
        val meter: SalaryUtilizationMeter = SalaryUtilizationMeter.compute(income, txs)

        assertEquals(60000.0, meter.monthlyIncome, 0.001)
        assertEquals(24000.0, meter.totalExpenditure, 0.001)
        assertEquals(36000.0, meter.remainingBalance, 0.001)
        // 24000 / 60000 * 100 = 40.0%
        assertEquals(40.0, meter.percentageSpent, 0.01)
        assertEquals(60.0, meter.percentageRemaining, 0.01)
        assertFalse(meter.isOverBudget)
    }

    @Test
    fun testKotlinInteroperabilityWithJavaPaymentProcessorAndExceptions() {
        val processor = DemoUpiPaymentProcessor()

        // 1. Valid payment execution from Kotlin
        val validRequest = PaymentRequest("merchant@demo", "Demo Merchant", 1200.0, "Shopping", "Shoes", "1234")
        val result = processor.processPayment(validRequest, 50000.0, "1234")
        assertTrue(result.isSuccessful)
        assertEquals(48800.0, result.updatedBalance, 0.001)
        assertNotNull(result.transaction)

        // 2. Exception handling from Kotlin: Invalid Amount
        val invalidAmountReq = PaymentRequest("merchant@demo", "Demo Merchant", -10.0, "Food", "", "1234")
        try {
            processor.processPayment(invalidAmountReq, 50000.0, "1234")
            fail("Must throw InvalidAmountException")
        } catch (e: InvalidAmountException) {
            assertEquals(-10.0, e.attemptedAmount, 0.001)
            assertEquals("ERR_INVALID_AMOUNT", e.errorCode)
        }

        // 3. Exception handling from Kotlin: Insufficient Funds
        val largeReq = PaymentRequest("car@demo", "Dealer", 90000.0, "Travel", "", "1234")
        try {
            processor.processPayment(largeReq, 20000.0, "1234")
            fail("Must throw InsufficientBalanceException")
        } catch (e: InsufficientBalanceException) {
            assertEquals(90000.0, e.attemptedAmount, 0.001)
            assertEquals(20000.0, e.availableBalance, 0.001)
        }

        // 4. Exception handling from Kotlin: Invalid PIN
        val wrongPinReq = PaymentRequest("store@demo", "Store", 500.0, "Food", "", "0000")
        try {
            processor.processPayment(wrongPinReq, 10000.0, "1234")
            fail("Must throw InvalidPinException")
        } catch (e: InvalidPinException) {
            assertEquals("ERR_INVALID_PIN", e.errorCode)
            assertEquals(2, e.remainingAttempts)
        }
    }

    @Test
    fun testKotlinInteroperabilityWithJavaEmergencyVault() {
        val vault = EmergencyVault(25000.0, "1234")
        assertEquals(25000.0, vault.reserveBalance, 0.001)

        // Deposit from Kotlin
        vault.deposit(5000.0)
        assertEquals(30000.0, vault.reserveBalance, 0.001)

        // Withdraw from Kotlin
        val withdrawn = vault.withdraw(10000.0, "1234")
        assertEquals(10000.0, withdrawn, 0.001)
        assertEquals(20000.0, vault.reserveBalance, 0.001)
        assertFalse(vault.isLocked)
    }

    @Test
    fun testFinancialEngineSummaryPoweredByJavaLogic() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val monthlyBudget = 50000.0
        val txs = listOf(
            TransactionEntity(
                id = 1,
                title = "Laptop Repair",
                recipientUpiId = "service@demo",
                recipientName = "Service Center",
                amount = 10000.0,
                category = "Shopping",
                timestamp = cal.timeInMillis,
                referenceId = "REF_1"
            )
        )

        val bills = listOf(
            BillEntity(
                id = 1,
                title = "Electricity",
                category = "Bills",
                amount = 2000.0,
                dueDayOfMonth = 15,
                isPaid = false
            )
        )

        val summary = FinancialEngine.calculateSummary(
            monthlyBudget = monthlyBudget,
            transactions = txs,
            bills = bills,
            recurringList = emptyList(),
            calendar = cal
        )

        // Total spent: 10000 out of 50000 = 20.0%
        assertEquals(50000.0, summary.monthlyBudget, 0.001)
        assertEquals(10000.0, summary.totalSpentThisMonth, 0.001)
        assertEquals(40000.0, summary.remainingBudget, 0.001)
        assertEquals(2000.0, summary.unpaidBillsReserved, 0.001)
        assertEquals(38000.0, summary.unreservedBalance, 0.001)
        assertEquals(20.0, summary.budgetUtilizationPercentage, 0.01)
        assertEquals(80.0, summary.percentageRemaining, 0.01)
        assertFalse(summary.isOverBudget)
    }
}
