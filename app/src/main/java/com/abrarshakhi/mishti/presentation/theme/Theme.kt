package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MishtiTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        content = content
    )
}