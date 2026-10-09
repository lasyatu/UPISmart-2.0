package com.example.domain.service

import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.FinancialSummary

data class BucksChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickSuggestions: List<String> = emptyList()
)

object BucksAssistantEngine {

    val INITIAL_GREETING = BucksChatMessage(
        isUser = false,
        text = "Yo! I'm Bucks, your personal money sidekick 💵 Ready to keep your wallet looking healthy without killing the vibe. What are we looking at today?",
        quickSuggestions = listOf(
            "Check my budget",
            "Analyze my spending",
            "Upcoming bills",
            "Savings goals",
            "How does QR pay work?"
        )
    )

    fun generateResponse(
        userQuery: String,
        summary: FinancialSummary,
        transactions: List<TransactionEntity>,
        bills: List<BillEntity>,
        goals: List<SavingsGoalEntity>
    ): BucksChatMessage {
        val q = userQuery.trim().lowercase()

        val text: String
        val suggestions = mutableListOf<String>()

        when {
            // Budget check
            q.contains("budget") || q.contains("safe to spend") || q.contains("safe-to-spend") || q.contains("balance") -> {
                val safePerDayStr = FinancialEngine.formatInr(summary.safeToSpendPerDay)
                val remainingStr = FinancialEngine.formatInr(summary.remainingBudget)
                val budgetStr = FinancialEngine.formatInr(summary.monthlyBudget)
                val spentStr = FinancialEngine.formatInr(summary.totalSpentThisMonth)
                val pct = summary.budgetUtilizationPercentage.toInt()

                text = when {
                    pct >= 80 -> "Big alert: you've tapped into $pct% of your $budgetStr budget ($spentStr spent) 💀 You've got $remainingStr left for the next ${summary.remainingDaysInMonth} days. Your Safe-to-Spend is $safePerDayStr/day right now, so let's keep impulsive Swiggy orders on ice!"
                    pct >= 50 -> "You're at $pct% budget usage. Out of your $budgetStr monthly cap, you've spent $spentStr. You've got $remainingStr left in the tank! Safe-to-Spend pace is looking solid at $safePerDayStr/day for the remaining ${summary.remainingDaysInMonth} days."
                    else -> "Looking clean! You've only used $pct% of your $budgetStr budget ($spentStr spent). Safe-to-Spend is sitting pretty at $safePerDayStr/day with ${summary.remainingDaysInMonth} days left in the month. Keep cooking! 🔥"
                }
                suggestions.addAll(listOf("Analyze my spending", "Upcoming bills", "Savings goals"))
            }

            // Spending analysis & Detective
            q.contains("spending") || q.contains("analyze") || q.contains("detective") || q.contains("category") -> {
                if (transactions.isEmpty()) {
                    text = "No transactions logged this month yet, fam! Your slate is completely clean. Make a simulated QR payment to kick off the insights."
                    suggestions.add("How does QR pay work?")
                } else {
                    val catTotals = transactions.groupBy { it.category }
                        .mapValues { it.value.sumOf { tx -> tx.amount } }
                    val topCat = catTotals.maxByOrNull { it.value }
                    val maxTx = transactions.maxByOrNull { it.amount }

                    val topCatStr = if (topCat != null) "${topCat.key} (${FinancialEngine.formatInr(topCat.value)})" else "None"
                    val maxTxStr = if (maxTx != null) "${maxTx.title} (${FinancialEngine.formatInr(maxTx.amount)})" else "None"

                    text = "Here's the breakdown, no cap 📊\n• Top spending category: $topCatStr\n• Biggest single debit: $maxTxStr\n• Total debit count: ${transactions.size} transactions.\n" +
                            if (summary.unpaidBillsReserved > 0) "Don't forget we also have ${FinancialEngine.formatInr(summary.unpaidBillsReserved)} reserved for bills!" else "No bills blocking your cashflow right now."
                    suggestions.addAll(listOf("Recent transactions", "Check my budget", "Savings goals"))
                }
            }

            // Recent transactions
            q.contains("recent") || q.contains("transaction") || q.contains("history") || q.contains("debit") -> {
                if (transactions.isEmpty()) {
                    text = "Zero transactions on file so far! Everything's quiet on the activity feed."
                    suggestions.add("How does QR pay work?")
                } else {
                    val recent3 = transactions.take(3)
                    val formatted = recent3.joinToString("\n") { tx ->
                        "• ${tx.title}: ${FinancialEngine.formatInr(tx.amount)} [${tx.category}]"
                    }
                    text = "Here's what went down recently:\n$formatted\n\nTotal debits logged: ${transactions.size}. You can search and filter everything in the Insights tab!"
                    suggestions.addAll(listOf("Check my budget", "Analyze my spending"))
                }
            }

            // Upcoming bills & Radar
            q.contains("bill") || q.contains("radar") || q.contains("due") || q.contains("subscription") -> {
                val unpaid = bills.filter { !it.isPaid }
                if (unpaid.isEmpty()) {
                    text = "All clear on Bills Radar! 🎉 Every bill in your schedule is marked paid. No extra money is being reserved."
                    suggestions.addAll(listOf("Check my budget", "Savings goals"))
                } else {
                    val billsList = unpaid.joinToString("\n") { "• ${it.title}: ${FinancialEngine.formatInr(it.amount)} (Due day ${it.dueDayOfMonth})" }
                    text = "Eyes up! You've got ${unpaid.size} upcoming bills reserving ${FinancialEngine.formatInr(summary.unpaidBillsReserved)}:\n$billsList\n\nYour Safe-to-Spend automatically excludes this so you never bounce a payment."
                    suggestions.addAll(listOf("Check my budget", "Savings goals"))
                }
            }

            // Savings goals / missions
            q.contains("saving") || q.contains("goal") || q.contains("mission") || q.contains("vault") -> {
                if (goals.isEmpty()) {
                    text = "No savings missions active right now! Want to build an emergency fund or stash cash for a new gadget? Head over to Missions & Bills to create one."
                    suggestions.addAll(listOf("Check my budget", "Upcoming bills"))
                } else {
                    val goalStatus = goals.joinToString("\n") { g ->
                        val pct = if (g.targetAmount > 0) ((g.savedAmount / g.targetAmount) * 100).toInt() else 0
                        "• ${g.name}: ${FinancialEngine.formatInr(g.savedAmount)} of ${FinancialEngine.formatInr(g.targetAmount)} ($pct%)"
                    }
                    text = "Your money missions are putting in work 💪\n$goalStatus\n\nConsistency is king. Even small weekly drops add up fast!"
                    suggestions.addAll(listOf("Check my budget", "Analyze my spending"))
                }
            }

            // QR scanning guide & Safety
            q.contains("qr") || q.contains("scan") || q.contains("pay") || q.contains("how to") -> {
                text = "Scan & Pay in UPISmart is 100% simulated and safe! 🛡️\n1. Open 'Scan & Pay'\n2. Point at a demo QR code (or tap 'Sample QR' / 'Manual Entry')\n3. Format: upismart://pay?recipient=name@demo\n4. Enter the amount & category\n5. Check the Safe-to-Spend impact and hit Confirm!\n\nNo real money is ever moved, and we never ask for your real UPI PIN. Perfect for testing your budget velocity."
                suggestions.addAll(listOf("Check my budget", "Analyze my spending"))
            }

            // Greetings & casual
            q.contains("hi") || q.contains("hello") || q.contains("hey") || q.contains("sup") || q.contains("yo") -> {
                text = "Yo! What's popping? I'm tapped into your UPISmart numbers. Want a quick budget vibe check or a breakdown of your recent damage?"
                suggestions.addAll(listOf("Check my budget", "Analyze my spending", "Upcoming bills"))
            }

            // Thanks
            q.contains("thanks") || q.contains("thank") || q.contains("appreciate") || q.contains("cool") -> {
                text = "Anytime fam! Keep that bag secure and holler whenever you need another budget check 👊"
                suggestions.addAll(listOf("Check my budget", "Upcoming bills"))
            }

            // Fallback general personal finance wisdom with Gen Z flair
            else -> {
                text = "Gotchu! Looking at your numbers right now, your Safe-to-Spend is ${FinancialEngine.formatInr(summary.safeToSpendPerDay)}/day and remaining budget is ${FinancialEngine.formatInr(summary.remainingBudget)}. " +
                        "General golden rule: keep needs at 50%, wants at 30%, and 20% into savings missions. Hit me with any specific questions about your budget, bills, or recent transactions!"
                suggestions.addAll(listOf("Check my budget", "Analyze my spending", "Upcoming bills", "Savings goals"))
            }
        }

        return BucksChatMessage(
            isUser = false,
            text = text,
            quickSuggestions = suggestions
        )
    }
}
