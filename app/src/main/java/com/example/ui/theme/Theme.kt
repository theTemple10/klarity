package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme: Midnight Navy, Sapphire Blue, Radiant Gold & Crisp White
private val KlarityDarkColorScheme = darkColorScheme(
    primary = KlarityGoldLight,
    onPrimary = NavyDeep,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = KlarityGoldLight,
    secondary = SapphireBlue,
    onSecondary = PureWhite,
    secondaryContainer = NavySurface,
    onSecondaryContainer = PureWhite,
    tertiary = KlarityGoldPrimary,
    onTertiary = NavyDeep,
    background = NavyDeep,
    onBackground = PureWhite,
    surface = NavySurface,
    onSurface = PureWhite,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = PureWhite,
    error = StatusAlertRed,
    onError = PureWhite,
    outline = NavyBorder,
    outlineVariant = NavySurfaceVariant
)

// Light Theme: Crisp Clean White, Royal Gold, Sapphire Blue & Midnight Navy text
private val KlarityLightColorScheme = lightColorScheme(
    primary = KlarityGoldPrimary,
    onPrimary = PureWhite,
    primaryContainer = KlarityGoldGlow,
    onPrimaryContainer = KlarityGoldDark,
    secondary = RoyalBlue,
    onSecondary = PureWhite,
    secondaryContainer = SoftBlueLight,
    onSecondaryContainer = RoyalBlue,
    tertiary = SapphireBlue,
    onTertiary = PureWhite,
    background = OffWhite,
    onBackground = MidnightBlue,
    surface = PureWhite,
    onSurface = MidnightBlue,
    surfaceVariant = SoftBlueLight,
    onSurfaceVariant = SlateDark,
    error = StatusAlertRed,
    onError = PureWhite,
    outline = SlateNavySubtle,
    outlineVariant = IceBlueTint
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) KlarityDarkColorScheme else KlarityLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
