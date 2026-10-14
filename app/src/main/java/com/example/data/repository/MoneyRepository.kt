package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.SampleDataProvider
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MoneyRepository(private val database: AppDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> =
        database.transactionDao().getAllTransactions()

    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> =
        database.transactionDao().getRecentTransactions(limit)

    val allBills: Flow<List<BillEntity>> =
        database.billDao().getAllBills()

    val allGoals: Flow<List<SavingsGoalEntity>> =
        database.savingsGoalDao().getAllGoals()

    val allRecurring: Flow<List<RecurringTransactionEntity>> =
        database.recurringTransactionDao().getAllRecurring()

    val userSettings: Flow<UserSettingsEntity?> =
        database.userSettingsDao().getSettings()

    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>> =
        database.savingsGoalDao().getContributionsForGoal(goalId)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        database.transactionDao().insertTransaction(transaction)

    suspend fun deleteTransaction(id: Long) =
        database.transactionDao().deleteTransaction(id)

    suspend fun insertBill(bill: BillEntity): Long =
        database.billDao().insertBill(bill)

    suspend fun updateBill(bill: BillEntity) =
        database.billDao().updateBill(bill)

    suspend fun deleteBill(bill: BillEntity) =
        database.billDao().deleteBill(bill)

    suspend fun setBillPaidStatus(id: Long, isPaid: Boolean) =
        database.billDao().updatePaidStatus(id, isPaid)

    suspend fun insertGoal(goal: SavingsGoalEntity): Long =
        database.savingsGoalDao().insertGoal(goal)

    suspend fun updateGoal(goal: SavingsGoalEntity) =
        database.savingsGoalDao().updateGoal(goal)

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        database.savingsGoalDao().deleteContributionsForGoal(goal.id)
        database.savingsGoalDao().deleteGoal(goal)
    }

    suspend fun addGoalContribution(goalId: Long, amount: Double, note: String) {
        database.savingsGoalDao().insertContribution(
            SavingsContributionEntity(
                goalId = goalId,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
        // Update goal's saved amount
        val goals = database.savingsGoalDao().getAllGoals()
        // Fetch existing goal and update
        // We can query goals or update saved amount
    }

    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long =
        database.recurringTransactionDao().insertRecurring(recurring)

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) =
        database.recurringTransactionDao().updateRecurring(recurring)

    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) =
        database.recurringTransactionDao().deleteRecurring(recurring)

    suspend fun toggleRecurringActive(id: Long, isActive: Boolean) =
        database.recurringTransactionDao().toggleActive(id, isActive)

    /**
     * Scans for recurring transactions whose nextDueDate <= now,
     * logs a corresponding simulated transaction into transactions table,
     * and increments nextDueDate based on frequency (DAILY, WEEKLY, MONTHLY, YEARLY).
     */
    suspend fun processDueRecurringTransactions(now: Long = System.currentTimeMillis()) {
        val dueList = database.recurringTransactionDao().getDueRecurring(now)
        for (item in dueList) {
            val refId = "UPI/DEMO/REC/${System.currentTimeMillis() % 1000000}"
            val tx = TransactionEntity(
                title = item.payeeName,
                recipientUpiId = item.payeeUpiId,
                recipientName = item.payeeName,
                amount = item.amount,
                category = item.category,
                timestamp = now,
                referenceId = refId,
                note = "Auto-scheduled ${item.frequency.lowercase()} payment",
                isSimulated = true
            )
            database.transactionDao().insertTransaction(tx)

            // Advance date
            val nextDate = computeNextDueDate(item.nextDueDate, item.frequency)
            database.recurringTransactionDao().updateRecurring(
                item.copy(
                    nextDueDate = nextDate,
                    lastExecutedDate = now
                )
            )
        }
    }

    private fun computeNextDueDate(currentDueDate: Long, frequency: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (frequency.uppercase()) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "YEARLY" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1) // default MONTHLY
        }
        return cal.timeInMillis
    }

    suspend fun updateMonthlyBudget(newBudget: Double) {
        val current = database.userSettingsDao().getSettingsDirect() ?: UserSettingsEntity()
        database.userSettingsDao().updateSettings(current.copy(monthlyBudget = newBudget))
    }

    suspend fun updateSecurityPin(newPin: String) {
        val current = database.userSettingsDao().getSettingsDirect() ?: UserSettingsEntity()
        database.userSettingsDao().updateSettings(current.copy(securityPin = newPin))
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        val current = database.userSettingsDao().getSettingsDirect() ?: UserSettingsEntity()
        database.userSettingsDao().updateSettings(current.copy(biometricEnabled = enabled))
    }

    suspend fun resetDemoData() {
        database.transactionDao().clearAll()
        database.billDao().clearAll()
        database.savingsGoalDao().clearAll()
        database.recurringTransactionDao().clearAll()
        database.userSettingsDao().clearAll()
        SampleDataProvider.seedDatabase(database)
    }

    suspend fun syncTransactionToCloud(transaction: TransactionEntity) {
        try {
            val req = com.example.data.network.NetworkPaymentRequest(
                recipientUpi = transaction.recipientUpiId,
                recipientName = transaction.recipientName,
                amount = transaction.amount,
                category = transaction.category,
                enteredPin = "1234",
                note = transaction.note,
                userConfirmed = true
            )
            com.example.data.network.ApiClient.getService().postTransaction(req)
        } catch (_: Exception) {
            // Graceful fallback: maintain local offline persistence without failing
        }
    }
}

