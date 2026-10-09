package com.example.domain.service

import com.example.core.budget.BudgetManager
import com.example.core.budget.SalaryUtilizationMeter
import com.example.core.model.Transaction as JavaTransaction
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.FinancialSummary
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object FinancialEngine {

    /**
     * Calculates the remaining days in the current calendar month (inclusive of today).
     */
    fun getRemainingDaysInMonth(calendar: Calendar = Calendar.getInstance()): Int {
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return maxOf(1, (maxDays - currentDay + 1))
    }

    /**
     * Checks if a timestamp falls within the current calendar month and year.
     */
    fun isTimestampInCurrentMonth(timestamp: Long, currentCal: Calendar = Calendar.getInstance()): Boolean {
        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return txCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)
    }

    /**
     * Converts Kotlin TransactionEntity to Java Transaction model.
     */
    fun toJavaTransaction(entity: TransactionEntity): JavaTransaction {
        return JavaTransaction(
            entity.id,
            entity.title,
            entity.recipientUpiId,
            entity.recipientName,
            entity.amount,
            entity.category,
            entity.timestamp,
            entity.status,
            entity.referenceId,
            entity.note,
            entity.isSimulated
        )
    }

    /**
     * Computes the complete financial summary with decimal-safe arithmetic,
     * powered directly by the Core Java BudgetManager and SalaryUtilizationMeter.
     */
    fun calculateSummary(
        monthlyBudget: Double,
        transactions: List<TransactionEntity>,
        bills: List<BillEntity>,
        recurringList: List<RecurringTransactionEntity> = emptyList(),
        calendar: Calendar = Calendar.getInstance()
    ): FinancialSummary {
        // Filter to current month successful transactions
        val currentMonthEntities = transactions.filter {
            it.status == "SUCCESS" && isTimestampInCurrentMonth(it.timestamp, calendar)
        }
        val javaTransactions = currentMonthEntities.map { toJavaTransaction(it) }

        // Compute 100% Salary Utilization Meter via Java business logic
        val meter = SalaryUtilizationMeter.compute(monthlyBudget, javaTransactions)

        // Reserved money from unpaid bills only (paid bills are never reserved twice)
        val unpaidBillsBD = bills
            .filter { !it.isPaid }
            .fold(BigDecimal.ZERO) { acc, bill ->
                acc.add(BigDecimal(bill.amount.toString()))
            }

        // Add active recurring transactions due in the remainder of this month
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val recurringReservationsBD = recurringList
            .filter { it.isActive }
            .fold(BigDecimal.ZERO) { acc, rec ->
                val recCal = Calendar.getInstance().apply { timeInMillis = rec.nextDueDate }
                val isDueThisMonth = recCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        recCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        recCal.get(Calendar.DAY_OF_MONTH) >= currentDay
                if (isDueThisMonth) acc.add(BigDecimal(rec.amount.toString())) else acc
            }

        val totalReserved = unpaidBillsBD.add(recurringReservationsBD)
            .setScale(2, RoundingMode.HALF_EVEN)
            .toDouble()

        // Delegate to Java BudgetManager for safe-to-spend
        val budgetManager = BudgetManager(monthlyBudget, totalReserved)
        budgetManager.setTransactions(javaTransactions)

        val remainingDays = getRemainingDaysInMonth(calendar)
        val safeToSpend = budgetManager.calculateSafeToSpendPerDay(remainingDays)
        val unreservedDisposable = budgetManager.unreservedDisposableBalance

        return FinancialSummary(
            monthlyBudget = meter.monthlyIncome,
            totalSpentThisMonth = meter.totalExpenditure,
            remainingBudget = meter.remainingBalance,
            unpaidBillsReserved = totalReserved,
            unreservedBalance = unreservedDisposable,
            remainingDaysInMonth = remainingDays,
            safeToSpendPerDay = safeToSpend,
            budgetUtilizationPercentage = meter.percentageSpent,
            percentageRemaining = meter.percentageRemaining,
            isOverBudget = meter.isOverBudget
        )
    }

    /**
     * Formats an amount into INR currency representation (e.g. ₹50,000 or ₹1,850.50).
     */
    fun formatInr(amount: Double, includeDecimals: Boolean = false): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            if (!includeDecimals) {
                format.maximumFractionDigits = 0
            }
            format.format(amount)
        } catch (e: Exception) {
            val rounded = if (includeDecimals) String.format(Locale.US, "%.2f", amount) else String.format(Locale.US, "%.0f", amount)
            "₹$rounded"
        }
    }
}
