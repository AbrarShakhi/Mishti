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
    const val md = 16   // updated: was 12, Flutter theme uses 16
    const val lg = 24
    const val xl = 32
    const val xxl = 48  // updated: was 24, Flutter theme uses 48
    const val xxxl = 80 // updated: was 32, Flutter theme uses 80

    const val pageHorizontal      = 20  // pageGutterMobile equivalent
    const val pageVertical        = 24
    const val inputHorizontal     = 16
    const val inputVertical       = 16
    const val buttonVertical      = 18
    const val cardPadding         = 12
    const val messageBubbleInset  = 80
}

object MishtiCornerRadius {
    const val sm     = 8    // button, input
    const val button = 8
    const val input  = 8
    const val md     = 12   // card, toast
    const val card   = 12
    const val toast  = 12
    const val lg     = 16
    const val xl     = 24
    const val pill   = 999
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
    primary          = MishtiColor.accentLight,
    onPrimary        = MishtiColor.onAccentLight,

    secondary        = MishtiColor.accentLight,
    onSecondary      = MishtiColor.onAccentLight,

    background       = MishtiColor.backgroundBaseLight,
    onBackground     = MishtiColor.textPrimaryLight,

    surface          = MishtiColor.surfaceLight,
    onSurface        = MishtiColor.textPrimaryLight,

    surfaceVariant   = MishtiColor.surfaceMutedLight,
    onSurfaceVariant = MishtiColor.textSecondaryLight,

    outline          = MishtiColor.borderLight,
    outlineVariant   = MishtiColor.borderLight,

    error            = MishtiColor.error
)

private fun darkMishtiColorScheme(): ColorScheme = darkColorScheme(
    primary          = MishtiColor.accentDark,
    onPrimary        = MishtiColor.onAccentDark,

    secondary        = MishtiColor.accentDark,
    onSecondary      = MishtiColor.onAccentDark,

    background       = MishtiColor.backgroundBaseDark,
    onBackground     = MishtiColor.textPrimaryDark,

    surface          = MishtiColor.surfaceDark,
    onSurface        = MishtiColor.textPrimaryDark,

    surfaceVariant   = MishtiColor.surfaceMutedDark,
    onSurfaceVariant = MishtiColor.textSecondaryDark,

    outline          = MishtiColor.borderDark,
    outlineVariant   = MishtiColor.borderDark,

    error            = MishtiColor.error
)