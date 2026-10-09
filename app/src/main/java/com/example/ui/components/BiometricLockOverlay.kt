package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet

@Composable
fun BiometricLockOverlay(
    isLocked: Boolean,
    biometricEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    if (!isLocked) return

    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var hasPromptedBiometrics by remember { mutableStateOf(false) }

    fun triggerBiometrics() {
        if (activity != null && biometricEnabled) {
            BiometricAuthManager.showBiometricPrompt(
                activity = activity,
                title = "UPISmart Biometric Unlock",
                subtitle = "Confirm your identity to view sensitive finances",
                onSuccess = {
                    pinError = null
                    onUnlockSuccess()
                },
                onError = { err ->
                    pinError = err
                },
                onFallbackPin = {
                    // Fallback to PIN display
                }
            )
        }
    }

    LaunchedEffect(isLocked, biometricEnabled) {
        if (isLocked && biometricEnabled && !hasPromptedBiometrics) {
            hasPromptedBiometrics = true
            triggerBiometrics()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("biometric_lock_overlay"),
        color = FintechBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Friendly Mascot Branding with Security Accent
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(FintechSurfaceVariant)
                        .border(2.dp, FintechNeonLime.copy(alpha = 0.7f), CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_upismart_mascot),
                        contentDescription = "UPISmart Mascot",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Security lock badge at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FintechCard)
                        .border(1.5.dp, FintechViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secured",
                        tint = FintechViolet,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Complete UPISmart 2.0 Wordmark Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_upismart_wordmark),
                contentDescription = "UPISmart 2.0 Wordmark",
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
                    .testTag("upismart_wordmark_splash")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Personal Money OS • Biometric Security",
                style = MaterialTheme.typography.bodySmall,
                color = FintechTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) FintechNeonLime else Color.Transparent)
                            .border(1.5.dp, if (isFilled) FintechNeonLime else FintechCardBorder, CircleShape)
                    )
                }
            }

            if (pinError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = pinError!!,
                    color = FintechDanger,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("bio", "0", "del")
                )

                for (row in keypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "bio" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(FintechSurfaceVariant)
                                            .clickable { triggerBiometrics() }
                                            .testTag("biometric_trigger_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Scan Fingerprint",
                                            tint = FintechViolet,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                "del" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(FintechSurfaceVariant)
                                            .clickable {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    pinError = null
                                                }
                                            }
                                            .testTag("pin_delete_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete digit",
                                            tint = FintechTextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(FintechCard)
                                            .border(1.dp, FintechCardBorder, CircleShape)
                                            .clickable {
                                                if (enteredPin.length < 4) {
                                                    val newPin = enteredPin + key
                                                    enteredPin = newPin
                                                    pinError = null
                                                    if (newPin.length == 4) {
                                                        val ok = onVerifyPin(newPin)
                                                        if (ok) {
                                                            onUnlockSuccess()
                                                        } else {
                                                            pinError = "Incorrect PIN. Default demo PIN is 1234."
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                            .testTag("pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = FintechTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Default Demo Passcode: 1234",
                style = MaterialTheme.typography.labelSmall,
                color = FintechNeonLime.copy(alpha = 0.8f)
            )
        }
    }
}
