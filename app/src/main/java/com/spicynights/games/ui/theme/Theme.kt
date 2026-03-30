package com.spicynights.games.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.spicynights.games.data.local.AppThemePreference

private val MidnightColors = darkColorScheme(
    primary = Color(0xFFFF2E6A),
    onPrimary = Color.White,
    secondary = Color(0xFFFFEB3B),
    onSecondary = Color(0xFF1A1020),
    tertiary = Color(0xFFE63946),
    background = Color(0xFF1A0A10),
    surface = Color(0xFF2A1218),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF3D1820),
    onSurfaceVariant = Color(0xFFE0E0E0),
)

private val TwilightColors = darkColorScheme(
    primary = Color(0xFFFF4D7D),
    onPrimary = Color.White,
    secondary = Color(0xFFFFF176),
    onSecondary = Color(0xFF1A1020),
    tertiary = Color(0xFFFF6B6B),
    background = Color(0xFF252030),
    surface = Color(0xFF353045),
    onBackground = Color(0xFFF0F0F5),
    onSurface = Color(0xFFF0F0F5),
    surfaceVariant = Color(0xFF454058),
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
