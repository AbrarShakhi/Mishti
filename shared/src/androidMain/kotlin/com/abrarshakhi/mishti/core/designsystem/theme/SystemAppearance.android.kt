package com.abrarshakhi.mishti.core.designsystem.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity
    DisposableEffect(isDark) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ) { isDark },
            navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { isDark },
        )
        onDispose {}
    }
}

private val LightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
