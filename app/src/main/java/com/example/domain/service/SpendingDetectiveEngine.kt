package com.example.domain.service

import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AlertSeverity
import com.example.domain.model.SpendingAlert
import java.util.Calendar

object SpendingDetectiveEngine {

    /**
     * Performs explainable rule-based spending analysis over transactions against monthly budget.
     */
    fun analyze(
        monthlyBudget: Double,
        transactions: List<TransactionEntity>,
        calendar: Calendar = Calendar.getInstance()
    ): List<SpendingAlert> {
        val alerts = mutableListOf<SpendingAlert>()

        val currentMonthTxs = transactions.filter {
            it.status == "SUCCESS" && FinancialEngine.isTimestampInCurrentMonth(it.timestamp, calendar)
        }

        val totalSpent = currentMonthTxs.sumOf { it.amount }

        // RULE 1: Flag single payment exceeding 20% of monthly budget
        val twentyPercentThreshold = monthlyBudget * 0.20
        val oversizedTxs = currentMonthTxs.filter { it.amount >= twentyPercentThreshold }
        if (oversizedTxs.isNotEmpty()) {
            val biggest = oversizedTxs.maxByOrNull { it.amount }!!
            val pct = (biggest.amount / monthlyBudget * 100).toInt()
            alerts.add(
                SpendingAlert(
                    id = "high_impact_tx",
                    title = "High Impact Transaction Detected",
                    message = "${biggest.title} used $pct% of your total monthly budget in a single payment.",
                    explanation = "Rule: Single payment ≥ 20% threshold (${FinancialEngine.formatInr(twentyPercentThreshold)}). " +
                            "This transaction was ${FinancialEngine.formatInr(biggest.amount)} ($pct% of ${FinancialEngine.formatInr(monthlyBudget)}).",
                    severity = AlertSeverity.WARNING,
                    metricHighlight = "${FinancialEngine.formatInr(biggest.amount)} ($pct%)"
                )
            )
        }

        // RULE 2: 80% Monthly Budget Threshold Warning
        val eightyPercentThreshold = monthlyBudget * 0.80
        if (monthlyBudget > 0 && totalSpent >= eightyPercentThreshold) {
            val utilization = (totalSpent / monthlyBudget * 100).toInt()
            val remaining = maxOf(0.0, monthlyBudget - totalSpent)
            alerts.add(
                SpendingAlert(
                    id = "budget_80_percent",
                    title = "Budget Velocity Alert (≥80% Consumed)",
                    message = "You have consumed $utilization% of your monthly budget with ${FinancialEngine.getRemainingDaysInMonth(calendar)} days remaining.",
                    explanation = "Rule: Total spending ≥ 80% of budget (${FinancialEngine.formatInr(eightyPercentThreshold)}). " +
                            "Spent so far: ${FinancialEngine.formatInr(totalSpent)}. Remaining safe buffer: ${FinancialEngine.formatInr(remaining)}.",
                    severity = if (totalSpent >= monthlyBudget) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                    metricHighlight = "$utilization% Utilized"
                )
            )
        }

        // RULE 3: Highest-spending category identification
        if (currentMonthTxs.isNotEmpty()) {
            val categoryTotals = currentMonthTxs.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val topCategoryEntry = categoryTotals.maxByOrNull { it.value }
            if (topCategoryEntry != null && topCategoryEntry.value > 0) {
                val catSpent = topCategoryEntry.value
                val catPct = if (totalSpent > 0) (catSpent / totalSpent * 100).toInt() else 0
                alerts.add(
                    SpendingAlert(
                        id = "top_category",
                        title = "Top Spending Category: ${topCategoryEntry.key}",
                        message = "${topCategoryEntry.key} accounts for $catPct% of all spending this month.",
                        explanation = "Rule: Category with largest sum of debits. " +
                                "Total in ${topCategoryEntry.key}: ${FinancialEngine.formatInr(catSpent)} across ${currentMonthTxs.count { it.category == topCategoryEntry.key }} transactions out of ${FinancialEngine.formatInr(totalSpent)} total.",
                        severity = AlertSeverity.INFO,
                        metricHighlight = "${FinancialEngine.formatInr(catSpent)} ($catPct%)"
                    )
                )
            }
        }

        // RULE 4: Largest individual transaction
        val largestTx = currentMonthTxs.maxByOrNull { it.amount }
        if (largestTx != null && alerts.none { it.id == "high_impact_tx" }) {
            alerts.add(
                SpendingAlert(
                    id = "largest_tx",
                    title = "Largest Debited Payment",
                    message = "${largestTx.title} (${FinancialEngine.formatInr(largestTx.amount)}) is your highest single debit.",
                    explanation = "Rule: Transaction with peak amount in current month. " +
                            "Paid to ${largestTx.recipientName} (${largestTx.recipientUpiId}) under category '${largestTx.category}'.",
                    severity = AlertSeverity.INFO,
                    metricHighlight = FinancialEngine.formatInr(largestTx.amount)
                )
            )
        }

        return alerts
    }

    /**
     * Checks if a pending transaction will trigger any spending warnings prior to confirmation.
     */
    fun checkPrePaymentWarning(
        proposedAmount: Double,
        monthlyBudget: Double,
        remainingBudget: Double
    ): String? {
        val twentyPct = monthlyBudget * 0.20
        if (proposedAmount > remainingBudget) {
            return "Warning: This payment (${FinancialEngine.formatInr(proposedAmount)}) exceeds your remaining unreserved budget (${FinancialEngine.formatInr(remainingBudget)})."
        }
        if (proposedAmount >= twentyPct) {
            val pct = (proposedAmount / monthlyBudget * 100).toInt()
            return "Spending Detective Alert: This payment is $pct% of your total monthly budget (${FinancialEngine.formatInr(proposedAmount)} of ${FinancialEngine.formatInr(monthlyBudget)})."
        }
        return null
    }
}
