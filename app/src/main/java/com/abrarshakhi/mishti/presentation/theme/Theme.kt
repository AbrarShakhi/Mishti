package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MishtiSpacing {
    const val xs = 4
    const val sm = 8
    const val md = 12
    const val lg = 16
    const val xl = 20
    const val xxl = 24
    const val xxxl = 32

    const val pageHorizontal = 24
    const val pageVertical = 24
    const val inputHorizontal = 16
    const val inputVertical = 16
    const val buttonVertical = 18
    const val cardPadding = 12
    const val messageBubbleInset = 80
}

object MishtiCornerRadius {
    const val button = 8
    const val input = 8
    const val card = 12
    const val toast = 12
    const val codeBlock = 10
}

@Composable
fun MishtiTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) darkMishtiColorScheme() else lightMishtiColorScheme(),
        typography = MishtiTypography.material,
        content = content
    )
}

private fun lightMishtiColorScheme(): ColorScheme = lightColorScheme(
    primary = MishtiColor.accentLight,
    onPrimary = Color.Black,

    secondary = MishtiColor.fillFaintLight,
    onSecondary = MishtiColor.textPrimaryLight,

    background = MishtiColor.backgroundBaseLight,
    onBackground = MishtiColor.textPrimaryLight,

    surface = MishtiColor.fillFaintLight,
    onSurface = MishtiColor.textPrimaryLight,

    // Important for default M3 components (e.g. TextField placeholder/label colors).
    // If not set, Material uses its default palette (purple-ish).
    surfaceVariant = MishtiColor.fillFaintLight,
    onSurfaceVariant = MishtiColor.textMutedLight,

    outline = MishtiColor.borderLight,
    outlineVariant = MishtiColor.borderLight,

    error = MishtiColor.error
)

private fun darkMishtiColorScheme(): ColorScheme = darkColorScheme(
    primary = MishtiColor.accentDark,
    onPrimary = Color.Black,

    secondary = MishtiColor.fillFaintDark,
    onSecondary = MishtiColor.textPrimaryDark,

    background = MishtiColor.backgroundBaseDark,
    onBackground = MishtiColor.textPrimaryDark,

    surface = MishtiColor.fillFaintDark,
    onSurface = MishtiColor.textPrimaryDark,

    surfaceVariant = MishtiColor.fillFaintDark,
    onSurfaceVariant = MishtiColor.textMutedDark,

    outline = MishtiColor.borderDark,
    outlineVariant = MishtiColor.borderDark,

    error = MishtiColor.error
)