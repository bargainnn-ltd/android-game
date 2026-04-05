package com.application.eatbts.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.application.eatbts.data.local.AppThemePreference
import com.application.eatbts.ui.hub.HubLandingColors

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

private val LightColors = lightColorScheme(
    primary = Color(0xFFC2185B),
    onPrimary = Color.White,
    secondary = Color(0xFF006978),
    onSecondary = Color.White,
    tertiary = Color(0xFF7B1FA2),
    background = Color.White,
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE8E8EE),
    onSurfaceVariant = Color(0xFF45454F),
)

@Composable
fun CoupleGamesTheme(
    appTheme: AppThemePreference = AppThemePreference.MIDNIGHT,
    content: @Composable () -> Unit,
) {
    val scheme = when (appTheme) {
        AppThemePreference.MIDNIGHT -> MidnightColors
        AppThemePreference.LIGHT -> LightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = CoupleGamesTypography,
        content = content,
    )
}

@Composable
fun themeIsLight(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f

/** Full-screen stacks: neon (dark) or Material surfaces (light). */
@Composable
fun themeScreenBackgroundBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) {
        Brush.verticalGradient(
            listOf(scheme.background, scheme.surfaceVariant, scheme.background),
        )
    } else {
        NeonTokens.screenBackgroundBrush()
    }
}

/** Hub / gameplay landing gradient (matches previous dark hub when midnight). */
@Composable
fun themeHubLandingBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) {
        Brush.verticalGradient(
            listOf(scheme.background, scheme.surfaceVariant, scheme.background),
        )
    } else {
        Brush.verticalGradient(
            listOf(HubLandingColors.Black, HubLandingColors.Charcoal, HubLandingColors.Black),
        )
    }
}

@Composable
fun themeHubPrimaryText(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) scheme.onBackground else HubLandingColors.White
}

@Composable
fun themeHubSecondaryText(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) scheme.onSurfaceVariant else HubLandingColors.BodyGrey
}

@Composable
fun themeHubTertiaryText(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) scheme.onSurfaceVariant else HubLandingColors.TextDim
}

@Composable
fun themeHubCardSurface(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) scheme.surface else HubLandingColors.Surface
}

@Composable
fun themeHubCardElevated(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) scheme.surfaceVariant else HubLandingColors.SurfaceElevated
}

@Composable
fun themeNeverCardInnerBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return if (themeIsLight()) {
        Brush.verticalGradient(listOf(scheme.surface, scheme.surfaceVariant))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF1A1520), HubLandingColors.Surface))
    }
}
