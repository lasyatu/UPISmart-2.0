package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FintechColorScheme = darkColorScheme(
    primary = FintechNeonLime,
    onPrimary = FintechNeonLimeDark,
    primaryContainer = Color(0xFF263812),
    onPrimaryContainer = FintechNeonLime,
    secondary = FintechViolet,
    onSecondary = Color(0xFF1E143D),
    secondaryContainer = Color(0xFF2E2254),
    onSecondaryContainer = FintechViolet,
    tertiary = FintechCyan,
    onTertiary = Color(0xFF00363A),
    background = FintechBackground,
    onBackground = FintechTextPrimary,
    surface = FintechSurface,
    onSurface = FintechTextPrimary,
    surfaceVariant = FintechSurfaceVariant,
    onSurfaceVariant = FintechTextSecondary,
    outline = FintechCardBorder,
    error = FintechDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FintechColorScheme,
        typography = Typography,
        content = content
    )
}

