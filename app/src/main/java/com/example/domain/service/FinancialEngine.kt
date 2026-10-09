package com.example.domain.service

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
     * Computes the complete financial summary with decimal-safe arithmetic.
     */
    fun calculateSummary(
        monthlyBudget: Double,
        transactions: List<TransactionEntity>,
        bills: List<BillEntity>,
        recurringList: List<RecurringTransactionEntity> = emptyList(),
        calendar: Calendar = Calendar.getInstance()
    ): FinancialSummary {
        val budgetBD = BigDecimal(monthlyBudget.toString())

        // Sum current month transactions (status == SUCCESS)
        val currentMonthSpentBD = transactions
            .filter { it.status == "SUCCESS" && isTimestampInCurrentMonth(it.timestamp, calendar) }
            .fold(BigDecimal.ZERO) { acc, tx ->
                acc.add(BigDecimal(tx.amount.toString()))
            }

        // Remaining Budget = Monthly Budget - Current Month Spent
        val remainingBudgetBD = budgetBD.subtract(currentMonthSpentBD)

        // Reserved money from unpaid bills only (paid bills are never reserved twice)
        val unpaidBillsBD = bills
            .filter { !it.isPaid }
            .fold(BigDecimal.ZERO) { acc, bill ->
                acc.add(BigDecimal(bill.amount.toString()))
            }

        // Add active recurring transactions due in the remainder of this month
        val currentMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
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

        val totalReservedBD = unpaidBillsBD.add(recurringReservationsBD)

        // Unreserved Balance = Remaining Budget - Total Reserved
        val unreservedBalanceBD = remainingBudgetBD.subtract(totalReservedBD)

        val remainingDays = getRemainingDaysInMonth(calendar)
        val remainingDaysBD = BigDecimal(remainingDays.toString())

        // Safe-to-Spend per day = max(0, unreservedBalance / remainingDays)
        val safeToSpendBD = if (unreservedBalanceBD > BigDecimal.ZERO && remainingDays > 0) {
            unreservedBalanceBD.divide(remainingDaysBD, 2, RoundingMode.HALF_EVEN)
        } else {
            BigDecimal.ZERO
        }

        // Budget Utilization Percentage = (Spent / Budget) * 100
        val utilizationPercent = if (budgetBD > BigDecimal.ZERO) {
            currentMonthSpentBD.multiply(BigDecimal("100"))
                .divide(budgetBD, 2, RoundingMode.HALF_EVEN)
                .toDouble()
        } else {
            0.0
        }

        return FinancialSummary(
            monthlyBudget = budgetBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            totalSpentThisMonth = currentMonthSpentBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            remainingBudget = remainingBudgetBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            unpaidBillsReserved = totalReservedBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            unreservedBalance = unreservedBalanceBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            remainingDaysInMonth = remainingDays,
            safeToSpendPerDay = safeToSpendBD.setScale(2, RoundingMode.HALF_EVEN).toDouble(),
            budgetUtilizationPercentage = utilizationPercent
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
