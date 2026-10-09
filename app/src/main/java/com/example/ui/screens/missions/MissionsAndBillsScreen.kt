package com.example.ui.screens.missions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import com.example.core.model.VaultAuditLog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.domain.model.FinancialSummary
import com.example.domain.service.FinancialEngine
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechSuccess
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MissionsAndBillsScreen(
    summary: FinancialSummary,
    goals: List<SavingsGoalEntity>,
    bills: List<BillEntity>,
    recurring: List<RecurringTransactionEntity>,
    onAddGoal: (String, Double, String, String) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onContributeToGoal: (SavingsGoalEntity, Double, String) -> Unit,
    onAddBill: (String, Double, Int, String, String) -> Unit,
    onToggleBillPaid: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    onAddRecurring: (String, String, Double, String, String, Long, String) -> Unit,
    onToggleRecurringActive: (RecurringTransactionEntity) -> Unit,
    onDeleteRecurring: (RecurringTransactionEntity) -> Unit,
    vaultReserve: Double = 15000.0,
    isVaultLocked: Boolean = false,
    vaultAuditLogs: List<VaultAuditLog> = emptyList(),
    vaultErrorMessage: String? = null,
    onDepositVault: (Double) -> Boolean = { true },
    onWithdrawVault: (Double, String) -> Boolean = { _, _ -> true },
    onUnlockVault: (String) -> Boolean = { true },
    onClearVaultError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Missions, 1: Emergency Vault, 2: Bills & Recurring

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var goalToContribute by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var showDepositVaultDialog by remember { mutableStateOf(false) }
    var showWithdrawVaultDialog by remember { mutableStateOf(false) }
    var showUnlockVaultDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("missions_bills_screen")
    ) {
        // Tab Row Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = FintechBackground,
            contentColor = FintechNeonLime,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = FintechNeonLime
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Missions (${goals.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Vault Lock",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Bills (${bills.size})",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // TAB 0: Money Missions
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Savings Goals",
                                style = MaterialTheme.typography.titleLarge,
                                color = FintechTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target budgets locked into disciplined saving vaults",
                                style = MaterialTheme.typography.bodySmall,
                                color = FintechTextSecondary
                            )
                        }

                        Button(
                            onClick = { showAddGoalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_mission_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = FintechNeonLimeDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Mission", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (goals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No savings missions yet. Create one to stay accountable!", color = FintechTextSecondary)
                        }
                    }
                } else {
                    items(goals) { goal ->
                        GoalCardItem(
                            goal = goal,
                            onContributeClick = { goalToContribute = goal },
                            onDeleteClick = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: Emergency Vault Lock
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Emergency Reserve Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("emergency_vault_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FintechCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isVaultLocked) FintechDanger.copy(alpha = 0.6f) else FintechCyan.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isVaultLocked) Icons.Default.Lock else Icons.Default.Shield,
                                        contentDescription = "Vault Icon",
                                        tint = if (isVaultLocked) FintechDanger else FintechCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "EMERGENCY VAULT LOCK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isVaultLocked) FintechDanger else FintechCyan,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isVaultLocked) FintechDanger.copy(alpha = 0.2f) else FintechSuccess.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isVaultLocked) "LOCKED" else "RESERVE ACTIVE",
                                        color = if (isVaultLocked) FintechDanger else FintechSuccess,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = FinancialEngine.formatInr(vaultReserve, includeDecimals = true),
                                style = MaterialTheme.typography.displayMedium,
                                color = FintechTextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Separately tracked emergency reserve. Isolated from daily Safe-to-Spend calculations to prevent accidental depletion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FintechTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            if (vaultErrorMessage != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FintechDanger.copy(alpha = 0.15f))
                                        .border(1.dp, FintechDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = vaultErrorMessage,
                                        color = FintechDanger,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showDepositVaultDialog = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("vault_deposit_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FintechCyan)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Deposit", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (isVaultLocked) {
                                            showUnlockVaultDialog = true
                                        } else {
                                            showWithdrawVaultDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("vault_withdraw_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FintechSurfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Action",
                                        tint = FintechTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isVaultLocked) "Unlock Vault" else "Withdraw",
                                        color = FintechTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Audit Logs Section
                item {
                    Text(
                        text = "Vault Security Audit Trail",
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (vaultAuditLogs.isEmpty()) {
                    item {
                        Text(
                            text = "No audit log entries recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FintechTextSecondary
                        )
                    }
                } else {
                    items(vaultAuditLogs) { log ->
                        VaultAuditLogRow(log = log)
                    }
                }
            }
        } else {
            // TAB 1: Bills Radar & Recurring
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Reservation Summary Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FintechCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(FintechWarning.copy(alpha = 0.5f))
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "BILLS RESERVATION SUMMARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = FintechWarning,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${FinancialEngine.formatInr(summary.unpaidBillsReserved)} Protected",
                                style = MaterialTheme.typography.headlineMedium,
                                color = FintechTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "This amount is automatically subtracted from remaining budget before computing Safe-to-Spend.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FintechTextSecondary
                            )
                        }
                    }
                }

                // Bills Section Header + Action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scheduled Bills",
                            style = MaterialTheme.typography.titleMedium,
                            color = FintechTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showAddBillDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_bill_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Bill", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Bill", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (bills.isEmpty()) {
                    item {
                        Text("No scheduled bills in radar.", color = FintechTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    }
                } else {
                    items(bills) { bill ->
                        BillCardItem(
                            bill = bill,
                            onTogglePaid = { onToggleBillPaid(bill) },
                            onDelete = { onDeleteBill(bill) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Recurring Transactions Header + Action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recurring Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                color = FintechTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-scheduled debits on due dates",
                                style = MaterialTheme.typography.bodySmall,
                                color = FintechTextSecondary
                            )
                        }

                        Button(
                            onClick = { showAddRecurringDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_recurring_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Recurring", tint = FintechNeonLimeDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Auto", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (recurring.isEmpty()) {
                    item {
                        Text("No recurring transactions defined.", color = FintechTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    }
                } else {
                    items(recurring) { rec ->
                        RecurringCardItem(
                            rec = rec,
                            onToggleActive = { onToggleRecurringActive(rec) },
                            onDelete = { onDeleteRecurring(rec) }
                        )
                    }
                }
            }
        }

        // Add Goal Dialog
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { name, amount, month ->
                    showAddGoalDialog = false
                    onAddGoal(name, amount, month, "#C5FF65")
                }
            )
        }

        // Contribute to Goal Dialog
        if (goalToContribute != null) {
            ContributeGoalDialog(
                goal = goalToContribute!!,
                onDismiss = { goalToContribute = null },
                onConfirm = { amount, note ->
                    onContributeToGoal(goalToContribute!!, amount, note)
                    goalToContribute = null
                }
            )
        }

        // Add Bill Dialog
        if (showAddBillDialog) {
            AddBillDialog(
                onDismiss = { showAddBillDialog = false },
                onConfirm = { title, amount, dueDay, cat, notes ->
                    showAddBillDialog = false
                    onAddBill(title, amount, dueDay, cat, notes)
                }
            )
        }

        // Add Recurring Dialog
        if (showAddRecurringDialog) {
            AddRecurringDialog(
                onDismiss = { showAddRecurringDialog = false },
                onConfirm = { payee, upi, amt, cat, freq, due, note ->
                    showAddRecurringDialog = false
                    onAddRecurring(payee, upi, amt, cat, freq, due, note)
                }
            )
        }

        // Vault Deposit Dialog
        if (showDepositVaultDialog) {
            DepositVaultDialog(
                onDismiss = { showDepositVaultDialog = false },
                onConfirm = { amount ->
                    showDepositVaultDialog = false
                    onDepositVault(amount)
                }
            )
        }

        // Vault Withdraw Dialog
        if (showWithdrawVaultDialog) {
            WithdrawVaultDialog(
                onDismiss = { showWithdrawVaultDialog = false },
                onConfirm = { amount, pin ->
                    showWithdrawVaultDialog = false
                    onWithdrawVault(amount, pin)
                }
            )
        }

        // Vault Unlock Dialog
        if (showUnlockVaultDialog) {
            UnlockVaultDialog(
                onDismiss = { showUnlockVaultDialog = false },
                onConfirm = { pin ->
                    showUnlockVaultDialog = false
                    onUnlockVault(pin)
                }
            )
        }
    }
}

@Composable
private fun GoalCardItem(
    goal: SavingsGoalEntity,
    onContributeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val isCompleted = goal.isCompleted || goal.savedAmount >= goal.targetAmount

    Card(
        modifier = Modifier.fillMaxWidth().testTag("goal_card_${goal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isCompleted) FintechSuccess else FintechCardBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FintechSuccess.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("GOAL ACHIEVED 🎉", color = FintechSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FintechTextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${FinancialEngine.formatInr(goal.savedAmount)} saved",
                    style = MaterialTheme.typography.bodyLarge,
                    color = FintechNeonLime,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target: ${FinancialEngine.formatInr(goal.targetAmount)} (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = FintechTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) FintechSuccess else FintechNeonLime,
                trackColor = FintechSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target: ${goal.targetMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FintechTextSecondary
                )

                Button(
                    onClick = onContributeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FintechSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Add Contribution", color = FintechNeonLime, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun BillCardItem(
    bill: BillEntity,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val isOverdue = !bill.isPaid && bill.dueDayOfMonth < currentDay

    Card(
        modifier = Modifier.fillMaxWidth().testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isOverdue) FintechDanger.copy(alpha = 0.5f) else FintechCardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onTogglePaid,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Status",
                        tint = if (bill.isPaid) FintechSuccess else FintechTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bill.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = FintechTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isOverdue) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FintechDanger.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("OVERDUE", color = FintechDanger, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "Due on ${bill.dueDayOfMonth}th • ${if (bill.isPaid) "Paid" else "Reserved"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (bill.isPaid) FintechSuccess else FintechTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FinancialEngine.formatInr(bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FintechTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RecurringCardItem(
    rec: RecurringTransactionEntity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val nextDateStr = dateFormatter.format(Date(rec.nextDueDate))

    Card(
        modifier = Modifier.fillMaxWidth().testTag("recurring_item_${rec.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rec.payeeName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FintechViolet.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(rec.frequency, color = FintechViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Next: $nextDateStr • ${rec.payeeUpiId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FintechTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FinancialEngine.formatInr(rec.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = rec.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FintechNeonLime,
                        checkedTrackColor = FintechNeonLimeDark
                    )
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FintechTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var targetMonth by remember { mutableStateOf("Dec 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Money Mission", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Vacation Fund)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetMonth,
                    onValueChange = { targetMonth = it },
                    label = { Text("Target Date (e.g. Dec 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onConfirm(name, amt, targetMonth)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
            ) {
                Text("Create Mission", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun ContributeGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("Direct deposit") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to ${goal.name}", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current saved: ${FinancialEngine.formatInr(goal.savedAmount)} of ${FinancialEngine.formatInr(goal.targetAmount)}",
                    color = FintechTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Contribution Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
            ) {
                Text("Deposit Funds", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule New Bill", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title (e.g. WiFi Broadband)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it },
                    label = { Text("Due Day of Month (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val dueDay = dueDayStr.toIntOrNull() ?: 15
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, amt, dueDay.coerceIn(1, 31), "Bills", "")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechViolet)
            ) {
                Text("Add to Radar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String, Long, String) -> Unit
) {
    var payee by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("MONTHLY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Up Recurring Transaction", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee Name (e.g. Netflix Subscription)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Payee Demo UPI (e.g. sub@demo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
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
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val safeUpi = if (upiId.contains("@")) upiId else "$upiId@demo"
                    if (payee.isNotBlank() && amt > 0) {
                        val nextDue = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        onConfirm(payee, safeUpi, amt, "Bills", frequency, nextDue, "Auto-recurring")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
            ) {
                Text("Schedule Recurring", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun VaultAuditLogRow(log: VaultAuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (log.isSuccessful) FintechSuccess.copy(alpha = 0.15f) else FintechDanger.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.isSuccessful) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = "Status",
                        tint = if (log.isSuccessful) FintechSuccess else FintechDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.actionType.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (log.reason.isNotBlank()) log.reason else SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (log.amount > 0) {
                Text(
                    text = FinancialEngine.formatInr(log.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (log.actionType == VaultAuditLog.ActionType.DEPOSIT) FintechSuccess else FintechTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DepositVaultDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit to Emergency Vault", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stash funds into your separate emergency reserve buffer.", color = FintechTextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
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
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechCyan)
            ) {
                Text("Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun WithdrawVaultDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var pinStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw from Emergency Vault", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Emergency Reserve withdrawal requires entering your 4-digit security PIN.",
                    color = FintechTextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Withdrawal Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pinStr,
                    onValueChange = { if (it.length <= 4) pinStr = it },
                    label = { Text("4-Digit PIN (Default: 1234)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && pinStr.length >= 4) {
                        onConfirm(amt, pinStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechDanger)
            ) {
                Text("Confirm Withdrawal", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun UnlockVaultDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pinStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock Emergency Vault", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter your 4-digit security PIN to unlock the emergency reserve.",
                    color = FintechTextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = pinStr,
                    onValueChange = { if (it.length <= 4) pinStr = it },
                    label = { Text("4-Digit PIN (Default: 1234)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinStr.length >= 4) {
                        onConfirm(pinStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
            ) {
                Text("Unlock", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}
