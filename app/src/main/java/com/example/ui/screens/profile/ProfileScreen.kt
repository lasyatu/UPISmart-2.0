package com.example.ui.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.UserSettingsEntity
import com.example.domain.service.FinancialEngine
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet

@Composable
fun ProfileScreen(
    userSettings: UserSettingsEntity,
    onUpdateBudget: (Double) -> Unit,
    onToggleBiometrics: (Boolean) -> Unit,
    onUpdatePin: (String) -> Unit,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FintechCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(FintechNeonLime.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AR",
                            color = FintechNeonLime,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userSettings.userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = FintechTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userSettings.upiHandle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FintechTextSecondary
                        )
                    }
                }
            }
        }

        // Budget Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FintechCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "BUDGET & CURRENCY",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechNeonLime,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBudgetDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budget", tint = FintechTextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Monthly Budget Cap", color = FintechTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Calculates daily Safe-to-Spend", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Text(
                            text = FinancialEngine.formatInr(userSettings.monthlyBudget),
                            color = FintechNeonLime,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Default Currency", color = FintechTextPrimary, fontWeight = FontWeight.Medium)
                        Text("INR (₹ - Indian Rupee)", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Security Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FintechCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SECURITY & BIOMETRICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechViolet,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Biometrics", tint = FintechViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Authentication", color = FintechTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Fingerprint / Face lock on open", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = userSettings.biometricEnabled,
                            onCheckedChange = { onToggleBiometrics(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FintechViolet,
                                checkedTrackColor = FintechViolet.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Password, contentDescription = "PIN", tint = FintechTextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Fallback Security PIN", color = FintechTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Currently set to 4 digits", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Text("Change →", color = FintechViolet, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Demo Mode & Architecture Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FintechCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // UPISmart 2.0 Complete Brand Wordmark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_upismart_wordmark),
                            contentDescription = "UPISmart 2.0 Wordmark",
                            modifier = Modifier
                                .width(180.dp)
                                .height(38.dp)
                                .testTag("profile_upismart_wordmark")
                        )

                        Image(
                            painter = painterResource(id = R.drawable.ic_upismart_mascot),
                            contentDescription = "UPISmart Mascot",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechSurfaceVariant)
                                .padding(3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security Notice", tint = FintechNeonLime)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEMO & SAFETY SPECIFICATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = FintechNeonLime,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• All payments, accounts, and QR transactions are 100% simulated.\n" +
                                "• Never connected to NPCI, RBI, or live banking networks.\n" +
                                "• Zero real PINs or bank credentials are ever accepted or stored.\n" +
                                "• Full offline Room persistence and deterministic rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "UPISmart 2.0 • Native Kotlin & Compose • Room DB",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Reset Data Action
        item {
            Button(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_demo_data_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechSurfaceVariant)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = FintechDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Demo Data to Initial State", color = FintechDanger, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Edit Budget Dialog
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(userSettings.monthlyBudget.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Set Monthly Budget", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Budget in ₹ (e.g. 50000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newB = budgetInput.toDoubleOrNull() ?: 50000.0
                        if (newB > 0) {
                            onUpdateBudget(newB)
                            showBudgetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
                ) {
                    Text("Save", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel", color = FintechTextSecondary) }
            },
            containerColor = FintechCard
        )
    }

    // Change PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Change 4-Digit PIN", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("New 4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onUpdatePin(pinInput)
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
                ) {
                    Text("Save PIN", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel", color = FintechTextSecondary) }
            },
            containerColor = FintechCard
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Demo Data?", color = FintechDanger, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will restore the sample transactions, upcoming bills, and savings goals to their default presentation state. Proceed?",
                    color = FintechTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechDanger)
                ) {
                    Text("Reset Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel", color = FintechTextSecondary) }
            },
            containerColor = FintechCard
        )
    }
}
