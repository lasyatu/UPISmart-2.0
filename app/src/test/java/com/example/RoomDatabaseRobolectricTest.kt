package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.MoneyRepository
import com.example.domain.service.FinancialEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: MoneyRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MoneyRepository(db)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testTransactionInsertionAndBudgetDeduction() = runBlocking {
        // Save user settings
        db.userSettingsDao().updateSettings(
            UserSettingsEntity(
                id = 1,
                monthlyBudget = 50000.0,
                userName = "Test User"
            )
        )

        // Insert simulated transaction
        val tx = TransactionEntity(
            title = "Test Mart",
            recipientUpiId = "mart@demo",
            recipientName = "Test Mart",
            amount = 3500.0,
            category = "Food",
            timestamp = System.currentTimeMillis(),
            referenceId = "UPI/DEMO/TEST/123"
        )
        val id = repository.insertTransaction(tx)
        assertTrue(id > 0)

        val transactions = repository.allTransactions.first()
        assertEquals(1, transactions.size)
        assertEquals("Test Mart", transactions[0].title)
        assertEquals(3500.0, transactions[0].amount, 0.01)

        val summary = FinancialEngine.calculateSummary(
            monthlyBudget = 50000.0,
            transactions = transactions,
            bills = emptyList()
        )
        assertEquals(3500.0, summary.totalSpentThisMonth, 0.01)
        assertEquals(46500.0, summary.remainingBudget, 0.01)
    }

    @Test
    fun testBillPaidStatusUpdatesReservation() = runBlocking {
        val bill = BillEntity(
            id = 1,
            title = "Electricity",
            category = "Bills",
            amount = 2000.0,
            dueDayOfMonth = 20,
            isPaid = false
        )
        repository.insertBill(bill)

        var bills = repository.allBills.first()
        var summary = FinancialEngine.calculateSummary(
            monthlyBudget = 50000.0,
            transactions = emptyList(),
            bills = bills
        )
        // 2000 reserved
        assertEquals(2000.0, summary.unpaidBillsReserved, 0.01)
        assertEquals(48000.0, summary.unreservedBalance, 0.01)

        // Mark paid
        repository.setBillPaidStatus(bills[0].id, true)
        bills = repository.allBills.first()
        summary = FinancialEngine.calculateSummary(
            monthlyBudget = 50000.0,
            transactions = emptyList(),
            bills = bills
        )
        // Once paid, NOT reserved twice!
        assertEquals(0.0, summary.unpaidBillsReserved, 0.01)
        assertEquals(50000.0, summary.unreservedBalance, 0.01)
    }

    @Test
    fun testSavingsGoalContribution() = runBlocking {
        val goal = SavingsGoalEntity(
            name = "Bike Fund",
            targetAmount = 20000.0,
            savedAmount = 5000.0
        )
        val goalId = repository.insertGoal(goal)
        assertTrue(goalId > 0)

        repository.addGoalContribution(goalId, 2500.0, "Extra bonus")
        val contributions = repository.getContributionsForGoal(goalId).first()
        assertEquals(1, contributions.size)
        assertEquals(2500.0, contributions[0].amount, 0.01)
    }

    @Test
    fun testRecurringTransactionAutoExecution() = runBlocking {
        val now = System.currentTimeMillis()
        val rec = RecurringTransactionEntity(
            payeeName = "Streaming Pass",
            payeeUpiId = "stream@demo",
            amount = 499.0,
            category = "Entertainment",
            frequency = "MONTHLY",
            nextDueDate = now - 1000, // Due in the past -> must trigger!
            isActive = true
        )
        repository.insertRecurring(rec)

        // Process due
        repository.processDueRecurringTransactions(now)

        val txs = repository.allTransactions.first()
        assertEquals(1, txs.size)
        assertEquals("Streaming Pass", txs[0].title)
        assertEquals(499.0, txs[0].amount, 0.01)

        val updatedRecurring = repository.allRecurring.first()
        assertTrue(updatedRecurring[0].nextDueDate > now)
    }
}
