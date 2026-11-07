package com.abrarshakhi.mishti.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.core.designsystem.theme.rememberSeededColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme

@Composable
actual fun rememberThemeColorScheme(scheme: AppColorScheme, dark: Boolean): ColorScheme =
    rememberSeededColorScheme(scheme.seed, dark)
