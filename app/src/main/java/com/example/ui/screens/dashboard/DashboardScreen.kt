package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AlertSeverity
import com.example.domain.model.FinancialSummary
import com.example.domain.model.SpendingAlert
import com.example.domain.model.TransactionCategory
import com.example.domain.service.FinancialEngine
import com.example.ui.components.SafeToSpendGauge
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechSurface
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    summary: FinancialSummary,
    alerts: List<SpendingAlert>,
    recentTransactions: List<TransactionEntity>,
    bills: List<BillEntity>,
    goals: List<SavingsGoalEntity>,
    onNavigateToScanPay: () -> Unit,
    onNavigateToBucks: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onAddQuickTransaction: (String, Double, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showQuickAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section B: Safe-to-Spend Summary Card
        item {
            SafeToSpendGauge(summary = summary)
        }

        // Section C: Quick Actions Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_actions_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan & Pay",
                    accentColor = FintechNeonLime,
                    onClick = onNavigateToScanPay,
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    icon = Icons.Default.Add,
                    label = "Add Expense",
                    accentColor = FintechViolet,
                    onClick = { showQuickAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "View Budgets",
                    accentColor = FintechCyan,
                    onClick = onNavigateToMissions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section D: Compact Bucks AI Assistant Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBucks() }
                    .testTag("bucks_compact_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FintechCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FintechViolet.copy(alpha = 0.35f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FintechViolet.copy(alpha = 0.15f))
                                .border(1.dp, FintechViolet.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = FintechViolet,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bucks Assistant",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FintechTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FintechNeonLime.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI TIP",
                                        color = FintechNeonLime,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val tipText = if (summary.budgetUtilizationPercentage >= 80) {
                                "Pacing hot at ${summary.budgetUtilizationPercentage.toInt()}% budget used. Tap for breakdown."
                            } else {
                                "Safe pace: ${FinancialEngine.formatInr(summary.safeToSpendPerDay)}/day. Ask anything!"
                            }
                            Text(
                                text = tipText,
                                style = MaterialTheme.typography.bodySmall,
                                color = FintechTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Bucks",
                        tint = FintechViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section E: Spending Insights Preview (Max 1-2 compact cards)
        if (alerts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending Insights",
                            style = MaterialTheme.typography.titleMedium,
                            color = FintechTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "View all insights →",
                            style = MaterialTheme.typography.labelSmall,
                            color = FintechNeonLime,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToInsights() }
                        )
                    }

                    // Show only the top 1 most critical insight on dashboard to avoid clutter
                    val topAlert = alerts.first()
                    CompactInsightCard(alert = topAlert, onClick = onNavigateToInsights)
                }
            }
        }

        // Section F: Upcoming Bills & Savings Goals Preview
        val unpaidBills = bills.filter { !it.isPaid }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commitments & Goals",
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Manage →",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechTextSecondary,
                        modifier = Modifier.clickable { onNavigateToMissions() }
                    )
                }

                // Compact summary card for Bills + Savings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMissions() }
                        .testTag("commitments_preview_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FintechCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Bills line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(FintechWarning)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${unpaidBills.size} Upcoming Bills",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FintechTextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${FinancialEngine.formatInr(summary.unpaidBillsReserved)} Reserved",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FintechWarning,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        // Savings line
                        if (goals.isNotEmpty()) {
                            val activeGoal = goals.first()
                            val goalPct = if (activeGoal.targetAmount > 0) ((activeGoal.savedAmount / activeGoal.targetAmount) * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(FintechCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeGoal.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FintechTextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "$goalPct% of ${FinancialEngine.formatInr(activeGoal.targetAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FintechCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section G: Recent Transactions Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = "All Activity →",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechTextSecondary,
                    modifier = Modifier.clickable { onNavigateToInsights() }
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary
                    )
                }
            }
        } else {
            items(recentTransactions.take(3)) { tx ->
                TransactionRowItem(tx = tx, onClick = onNavigateToInsights)
            }
        }

        // Extra spacing at bottom to ensure no overlap with bottom bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Quick Add Expense Dialog
    if (showQuickAddDialog) {
        var expenseTitle by remember { mutableStateOf("") }
        var expenseAmount by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Food") }

        AlertDialog(
            onDismissRequest = { showQuickAddDialog = false },
            title = {
                Text("Log Expense", color = FintechTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Title (e.g. Lunch)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (expenseTitle.isNotBlank() && amt > 0) {
                            onAddQuickTransaction(expenseTitle, amt, selectedCategory)
                            showQuickAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
                ) {
                    Text("Record", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddDialog = false }) {
                    Text("Cancel", color = FintechTextSecondary)
                }
            },
            containerColor = FintechCard
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() }
            .testTag("quick_action_${label.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FintechTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactInsightCard(
    alert: SpendingAlert,
    onClick: () -> Unit
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> FintechDanger
        AlertSeverity.WARNING -> FintechWarning
        AlertSeverity.INFO -> FintechViolet
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("compact_insight_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor.copy(alpha = 0.4f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(borderColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Alert",
                        tint = borderColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(borderColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = alert.metricHighlight,
                    color = borderColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: TransactionEntity,
    onClick: (() -> Unit)? = null
) {
    val category = TransactionCategory.fromString(tx.category)
    val catColor = when (category) {
        TransactionCategory.FOOD -> FintechNeonLime
        TransactionCategory.TRAVEL -> FintechCyan
        TransactionCategory.SHOPPING -> FintechViolet
        TransactionCategory.BILLS -> FintechWarning
        else -> FintechTextSecondary
    }

    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    val dateStr = dateFormatter.format(Date(tx.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tx.category.take(1).uppercase(),
                        color = catColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "$dateStr • ${tx.recipientUpiId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "- ${FinancialEngine.formatInr(tx.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = FintechTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
