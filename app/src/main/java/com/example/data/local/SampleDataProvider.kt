package com.example.data.local

import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import java.util.UUID

object SampleDataProvider {

    suspend fun seedDatabase(database: AppDatabase) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        // Default User Settings
        database.userSettingsDao().updateSettings(
            UserSettingsEntity(
                id = 1,
                monthlyBudget = 50000.0,
                userName = "Alex Rivera",
                upiHandle = "alex@demo.upismart",
                biometricEnabled = true,
                securityPin = "1234",
                currencySymbol = "₹"
            )
        )

        // Seed Transactions
        val sampleTransactions = listOf(
            TransactionEntity(
                title = "Fresh Basket Supermarket",
                recipientUpiId = "merchant@demo",
                recipientName = "Demo Merchant",
                amount = 1850.0,
                category = "Food",
                timestamp = now - (oneDayMs * 1),
                referenceId = "UPI/DEMO/2026/894101",
                note = "Weekly groceries and pantry restock",
                isSimulated = true
            ),
            TransactionEntity(
                title = "City Metro Express",
                recipientUpiId = "transit@demo",
                recipientName = "Metro Transit",
                amount = 140.0,
                category = "Travel",
                timestamp = now - (oneDayMs * 2),
                referenceId = "UPI/DEMO/2026/774920",
                note = "Daily commute smart card reload",
                isSimulated = true
            ),
            TransactionEntity(
                title = "Decathlon Sports Gear",
                recipientUpiId = "sports@demo",
                recipientName = "Decathlon Retail",
                amount = 3499.0,
                category = "Shopping",
                timestamp = now - (oneDayMs * 3),
                referenceId = "UPI/DEMO/2026/663821",
                note = "Running shoes & sports apparel",
                isSimulated = true
            ),
            TransactionEntity(
                title = "FiberNet Broadband",
                recipientUpiId = "utility@demo",
                recipientName = "FiberNet Telecom",
                amount = 999.0,
                category = "Bills",
                timestamp = now - (oneDayMs * 5),
                referenceId = "UPI/DEMO/2026/552718",
                note = "Monthly gigabit fiber subscription",
                isSimulated = true
            ),
            TransactionEntity(
                title = "The Coffee Table Bistro",
                recipientUpiId = "bistro@demo",
                recipientName = "Coffee Table",
                amount = 450.0,
                category = "Food",
                timestamp = now - (oneDayMs * 6),
                referenceId = "UPI/DEMO/2026/441607",
                note = "Artisan latte and toast breakfast",
                isSimulated = true
            ),
            TransactionEntity(
                title = "Uber Airport Commute",
                recipientUpiId = "cab@demo",
                recipientName = "Uber Fleet",
                amount = 780.0,
                category = "Travel",
                timestamp = now - (oneDayMs * 8),
                referenceId = "UPI/DEMO/2026/330594",
                note = "Airport terminal transit",
                isSimulated = true
            )
        )

        for (tx in sampleTransactions) {
            database.transactionDao().insertTransaction(tx)
        }

        // Seed Bills
        val sampleBills = listOf(
            BillEntity(
                title = "Electricity Utility Bill",
                category = "Bills",
                amount = 2450.0,
                dueDayOfMonth = 18,
                isPaid = false,
                notes = "State power distribution account"
            ),
            BillEntity(
                title = "Apartment Maintenance",
                category = "Bills",
                amount = 4500.0,
                dueDayOfMonth = 25,
                isPaid = false,
                notes = "Society maintenance and clubhouse dues"
            ),
            BillEntity(
                title = "Water Supply Dues",
                category = "Bills",
                amount = 380.0,
                dueDayOfMonth = 10,
                isPaid = true,
                notes = "Municipal water supply bill"
            )
        )

        for (bill in sampleBills) {
            database.billDao().insertBill(bill)
        }

        // Seed Recurring Transactions
        val sampleRecurring = listOf(
            com.example.data.local.entity.RecurringTransactionEntity(
                payeeName = "Netflix Subscription",
                payeeUpiId = "netflix@demo",
                amount = 649.0,
                category = "Bills",
                frequency = "MONTHLY",
                nextDueDate = now + (oneDayMs * 4),
                isActive = true,
                notes = "Premium Ultra HD Plan"
            ),
            com.example.data.local.entity.RecurringTransactionEntity(
                payeeName = "Cult.fit Gym & Fitness",
                payeeUpiId = "gym@demo",
                amount = 1500.0,
                category = "Health",
                frequency = "MONTHLY",
                nextDueDate = now + (oneDayMs * 11),
                isActive = true,
                notes = "Monthly fitness club membership"
            )
        )
        for (rec in sampleRecurring) {
            database.recurringTransactionDao().insertRecurring(rec)
        }

        // Seed Savings Goals
        val goal1Id = database.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                name = "Emergency Fund",
                targetAmount = 25000.0,
                savedAmount = 14000.0,
                targetMonth = "Nov 2026",
                isCompleted = false,
                colorHex = "#C5FF65"
            )
        )

        val goal2Id = database.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                name = "Japan Vacation 2027",
                targetAmount = 60000.0,
                savedAmount = 18500.0,
                targetMonth = "Mar 2027",
                isCompleted = false,
                colorHex = "#B7A0FF"
            )
        )

        // Seed contributions for goals
        database.savingsGoalDao().insertContribution(
            SavingsContributionEntity(
                goalId = goal1Id,
                amount = 5000.0,
                timestamp = now - (oneDayMs * 12),
                note = "Monthly salary allocation"
            )
        )
        database.savingsGoalDao().insertContribution(
            SavingsContributionEntity(
                goalId = goal1Id,
                amount = 9000.0,
                timestamp = now - (oneDayMs * 4),
                note = "Bonus deposit"
            )
        )
        database.savingsGoalDao().insertContribution(
            SavingsContributionEntity(
                goalId = goal2Id,
                amount = 18500.0,
                timestamp = now - (oneDayMs * 10),
                note = "Initial travel fund"
            )
        )
    }

    const val SAMPLE_DEMO_QR_URI = "upismart://pay?recipient=merchant@demo&name=Demo%20Merchant"
    const val SAMPLE_DEMO_RECIPIENT = "merchant@demo"
    const val SAMPLE_DEMO_NAME = "Demo Merchant"
}
