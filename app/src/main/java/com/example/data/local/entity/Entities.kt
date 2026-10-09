package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val recipientUpiId: String,
    val recipientName: String,
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val referenceId: String,
    val note: String = "",
    val isSimulated: Boolean = true
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val dueDayOfMonth: Int, // e.g., 15 for 15th of the month
    val isPaid: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetMonth: String = "Dec 2026",
    val isCompleted: Boolean = false,
    val colorHex: String = "#C5FF65"
)

@Entity(tableName = "savings_contributions")
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "Contribution"
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payeeName: String,
    val payeeUpiId: String,
    val amount: Double,
    val category: String,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val nextDueDate: Long,
    val lastExecutedDate: Long? = null,
    val isActive: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyBudget: Double = 50000.0,
    val userName: String = "Alex Rivera",
    val upiHandle: String = "alex@demo.upismart",
    val biometricEnabled: Boolean = true,
    val securityPin: String = "1234",
    val currencySymbol: String = "₹"
)
