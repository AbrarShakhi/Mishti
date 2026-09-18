package com.abrarshakhi.mishti.common.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun MishtiTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val dark = when (settings.mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = if (settings.colorScheme == AppColorScheme.Dynamic && dynamicAvailable) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        settings.colorScheme.scheme(dark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typographyFor(settings.font.fontFamily()),
        content = content,
    )
}

val isDynamicColorAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
