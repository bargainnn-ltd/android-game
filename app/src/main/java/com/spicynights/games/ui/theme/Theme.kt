package com.spicynights.games.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.spicynights.games.data.local.AppThemePreference

private val MidnightColors = darkColorScheme(
    primary = NeonTokens.NeonMagenta,
    onPrimary = Color.White,
    secondary = NeonTokens.NeonCyan,
    onSecondary = Color(0xFF0A1218),
    tertiary = Color(0xFFE040FB),
    background = NeonTokens.BgDeep,
    surface = NeonTokens.BgElevated,
    onBackground = NeonTokens.TextPrimary,
    onSurface = NeonTokens.TextPrimary,
    surfaceVariant = Color(0xFF2A2A38),
    onSurfaceVariant = NeonTokens.TextMuted,
)

private val TwilightColors = darkColorScheme(
    primary = Color(0xFFFF4D7D),
    onPrimary = Color.White,
    secondary = NeonTokens.NeonCyan.copy(alpha = 0.95f),
    onSecondary = Color(0xFF0A1218),
    tertiary = Color(0xFFFF6B6B),
    background = Color(0xFF1E1A28),
    surface = Color(0xFF2E2A38),
    onBackground = Color(0xFFF0F0F5),
    onSurface = Color(0xFFF0F0F5),
    surfaceVariant = Color(0xFF3A3648),
    onSurfaceVariant = Color(0xFFD0D0DD),
)

@Composable
fun SpicyNightsTheme(
    appTheme: AppThemePreference = AppThemePreference.MIDNIGHT,
    content: @Composable () -> Unit,
) {
    val scheme = when (appTheme) {
        AppThemePreference.MIDNIGHT -> MidnightColors
        AppThemePreference.TWILIGHT -> TwilightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = SpicyNightsTypography,
        content = content,
    )
}
