package com.abrarshakhi.mishti.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme

val AppColorScheme.seed: Color
    get() = when (this) {
        AppColorScheme.Dynamic, AppColorScheme.Honey -> Color(0xFF865300)
        AppColorScheme.Indigo -> Color(0xFF4656B5)
        AppColorScheme.Forest -> Color(0xFF196C42)
        AppColorScheme.Rose -> Color(0xFFA3365E)
    }
