package com.abrarshakhi.mishti.common.ui.theme

import androidx.annotation.StringRes
import com.abrarshakhi.mishti.R

@get:StringRes
val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.System -> R.string.theme_system
        ThemeMode.Light -> R.string.theme_light
        ThemeMode.Dark -> R.string.theme_dark
    }

@get:StringRes
val AppColorScheme.labelRes: Int
    get() = when (this) {
        AppColorScheme.Dynamic -> R.string.palette_dynamic
        AppColorScheme.Honey -> R.string.palette_honey
        AppColorScheme.Indigo -> R.string.palette_indigo
        AppColorScheme.Forest -> R.string.palette_forest
        AppColorScheme.Rose -> R.string.palette_rose
    }

@get:StringRes
val AppFont.labelRes: Int
    get() = when (this) {
        AppFont.System -> R.string.font_system
        AppFont.Inter -> R.string.font_inter
        AppFont.Lora -> R.string.font_lora
        AppFont.JetBrainsMono -> R.string.font_jetbrains_mono
    }
