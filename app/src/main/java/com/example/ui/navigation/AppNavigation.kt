package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.components.FintechTopBar
import com.example.ui.screens.bucks.BucksChatScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.missions.MissionsAndBillsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.scanpay.ScanPayScreen
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.viewmodel.MainMoneyViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object ScanPay : Screen("scan_pay", "Scan & Pay", Icons.Default.QrCodeScanner)
    object Missions : Screen("missions", "Missions", Icons.Default.Savings)
    object Insights : Screen("insights", "Insights", Icons.Default.Insights)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Bucks : Screen("bucks", "Bucks AI", Icons.Default.AutoAwesome)
}

@Composable
fun AppNavigation(
    viewModel: MainMoneyViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val recurring by viewModel.recurring.collectAsStateWithLifecycle()
    val alerts by viewModel.spendingAlerts.collectAsStateWithLifecycle()
    val scanPayState by viewModel.scanPayState.collectAsStateWithLifecycle()
    val vaultReserve by viewModel.vaultReserve.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val vaultAuditLogs by viewModel.vaultAuditLogs.collectAsStateWithLifecycle()
    val vaultErrorMessage by viewModel.vaultErrorMessage.collectAsStateWithLifecycle()

    // Back button handling
    BackHandler(enabled = currentTab != Screen.Home.route) {
        viewModel.selectTab(Screen.Home.route)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (currentTab != Screen.ScanPay.route && currentTab != Screen.Bucks.route) {
                    FintechTopBar(
                        title = "UPISmart 2.0",
                        subtitle = "Safe-to-Spend Money OS",
                        onLockClick = { viewModel.lockApp() },
                        onBucksClick = { viewModel.selectTab(Screen.Bucks.route) }
                    )
                }
            },
            bottomBar = {
                if (currentTab != Screen.ScanPay.route && currentTab != Screen.Bucks.route) {
                    FintechBottomBar(
                        currentRoute = currentTab,
                        onTabSelected = { route -> viewModel.selectTab(route) }
                    )
                }
            },
            containerColor = FintechBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    Screen.Home.route -> {
                        DashboardScreen(
                            summary = summary,
                            alerts = alerts,
                            recentTransactions = recentTransactions,
                            bills = bills,
                            goals = goals,
                            onNavigateToScanPay = { viewModel.selectTab(Screen.ScanPay.route) },
                            onNavigateToBucks = { viewModel.selectTab(Screen.Bucks.route) },
                            onNavigateToInsights = { viewModel.selectTab(Screen.Insights.route) },
                            onNavigateToMissions = { viewModel.selectTab(Screen.Missions.route) },
                            onAddQuickTransaction = { title, amt, cat ->
                                viewModel.addManualTransaction(title, amt, cat)
                            }
                        )
                    }

                    Screen.ScanPay.route -> {
                        ScanPayScreen(
                            scanPayState = scanPayState,
                            summary = summary,
                            onQrScanned = { raw -> viewModel.onQrScanned(raw) },
                            onSetManualRecipient = { upi, name -> viewModel.setManualRecipient(upi, name) },
                            onAmountChanged = { amt -> viewModel.updatePaymentAmount(amt) },
                            onCategoryChanged = { cat -> viewModel.updatePaymentCategory(cat) },
                            onNoteChanged = { note -> viewModel.updatePaymentNote(note) },
                            onConfirmPayment = { callback -> viewModel.confirmSimulatedPayment(callback) },
                            onClearReceipt = { viewModel.clearPaymentReceipt() },
                            onClearError = { viewModel.clearScanError() },
                            onCancel = { viewModel.selectTab(Screen.Home.route) },
                            onViewInActivity = { viewModel.selectTab(Screen.Insights.route) }
                        )
                    }

                    Screen.Missions.route -> {
                        MissionsAndBillsScreen(
                            summary = summary,
                            goals = goals,
                            bills = bills,
                            recurring = recurring,
                            onAddGoal = { name, amt, month, color -> viewModel.addSavingsGoal(name, amt, month, color) },
                            onDeleteGoal = { g -> viewModel.deleteSavingsGoal(g) },
                            onContributeToGoal = { g, amt, note -> viewModel.contributeToGoal(g, amt, note) },
                            onAddBill = { title, amt, due, cat, notes -> viewModel.addBill(title, amt, due, cat, notes) },
                            onToggleBillPaid = { b -> viewModel.toggleBillPaid(b) },
                            onDeleteBill = { b -> viewModel.deleteBill(b) },
                            onAddRecurring = { payee, upi, amt, cat, freq, due, note ->
                                viewModel.addRecurringTransaction(payee, upi, amt, cat, freq, due, note)
                            },
                            onToggleRecurringActive = { r -> viewModel.toggleRecurringActive(r) },
                            onDeleteRecurring = { r -> viewModel.deleteRecurringTransaction(r) },
                            vaultReserve = vaultReserve,
                            isVaultLocked = isVaultLocked,
                            vaultAuditLogs = vaultAuditLogs,
                            vaultErrorMessage = vaultErrorMessage,
                            onDepositVault = { amt -> viewModel.depositToVault(amt) },
                            onWithdrawVault = { amt, pin -> viewModel.withdrawFromVault(amt, pin) },
                            onUnlockVault = { pin -> viewModel.unlockVault(pin) },
                            onClearVaultError = { viewModel.clearVaultError() }
                        )
                    }

                    Screen.Insights.route -> {
                        InsightsScreen(
                            summary = summary,
                            transactions = transactions
                        )
                    }

                    Screen.Profile.route -> {
                        ProfileScreen(
                            userSettings = userSettings,
                            onUpdateBudget = { b -> viewModel.updateMonthlyBudget(b) },
                            onToggleBiometrics = { e -> viewModel.toggleBiometricSetting(e) },
                            onUpdatePin = { p -> viewModel.updateSecurityPin(p) },
                            onResetDemoData = { viewModel.resetDemoData() }
                        )
                    }

                    Screen.Bucks.route -> {
                        BucksChatScreen(
                            summary = summary,
                            transactions = transactions,
                            bills = bills,
                            goals = goals,
                            onBack = { viewModel.selectTab(Screen.Home.route) }
                        )
                    }
                }
            }
        }

        // Biometric Lock Overlay (covers entire UI when locked)
        BiometricLockOverlay(
            isLocked = !isUnlocked,
            biometricEnabled = userSettings.biometricEnabled,
            onUnlockSuccess = { viewModel.unlockApp() },
            onVerifyPin = { pin -> viewModel.verifyPinAndUnlock(pin) }
        )
    }
}

@Composable
private fun FintechBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Screen.Home,
        Screen.ScanPay,
        Screen.Missions,
        Screen.Insights,
        Screen.Profile
    )

    NavigationBar(
        containerColor = FintechBackground,
        contentColor = FintechTextSecondary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = FintechCardBorder)
            .testTag("fintech_bottom_navigation")
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(screen.route) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FintechNeonLimeDark,
                    selectedTextColor = FintechNeonLime,
                    indicatorColor = FintechNeonLime,
                    unselectedIconColor = FintechTextSecondary,
                    unselectedTextColor = FintechTextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${screen.route}")
            )
        }
    }
}
