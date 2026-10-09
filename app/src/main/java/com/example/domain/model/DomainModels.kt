package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSuccess
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning

enum class TransactionCategory(val displayName: String, val colorHex: String) {
    FOOD("Food", "#C5FF65"),
    TRAVEL("Travel", "#48CAE4"),
    SHOPPING("Shopping", "#B7A0FF"),
    BILLS("Bills", "#FFB347"),
    ENTERTAINMENT("Entertainment", "#FF70A6"),
    HEALTH("Health", "#2EC4B6"),
    OTHER("Other", "#A0A0BA");

    companion object {
        fun fromString(name: String): TransactionCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHER
        }
    }
}

data class ParsedQrData(
    val recipientUpiId: String,
    val merchantName: String,
    val amount: Double? = null,
    val currency: String = "INR",
    val note: String = "",
    val isValid: Boolean,
    val errorMessage: String? = null,
    val rawUri: String = ""
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class SpendingAlert(
    val id: String,
    val title: String,
    val message: String,
    val explanation: String,
    val severity: AlertSeverity,
    val metricHighlight: String
)

data class FinancialSummary(
    val monthlyBudget: Double,
    val totalSpentThisMonth: Double,
    val remainingBudget: Double,
    val unpaidBillsReserved: Double,
    val unreservedBalance: Double,
    val remainingDaysInMonth: Int,
    val safeToSpendPerDay: Double,
    val budgetUtilizationPercentage: Double,
    val percentageRemaining: Double = if (monthlyBudget > 0.0) maxOf(0.0, 100.0 - budgetUtilizationPercentage) else 0.0,
    val isOverBudget: Boolean = totalSpentThisMonth > monthlyBudget
)
