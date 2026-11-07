package com.abrarshakhi.mishti.settings.ui

import com.abrarshakhi.mishti.core.designsystem.theme.AppColorScheme
import com.abrarshakhi.mishti.core.designsystem.theme.AppFont
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeMode
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.font_inter
import com.abrarshakhi.mishti.shared.resources.font_jetbrains_mono
import com.abrarshakhi.mishti.shared.resources.font_lora
import com.abrarshakhi.mishti.shared.resources.font_system
import com.abrarshakhi.mishti.shared.resources.palette_dynamic
import com.abrarshakhi.mishti.shared.resources.palette_forest
import com.abrarshakhi.mishti.shared.resources.palette_honey
import com.abrarshakhi.mishti.shared.resources.palette_indigo
import com.abrarshakhi.mishti.shared.resources.palette_rose
import com.abrarshakhi.mishti.shared.resources.theme_dark
import com.abrarshakhi.mishti.shared.resources.theme_light
import com.abrarshakhi.mishti.shared.resources.theme_system
import org.jetbrains.compose.resources.StringResource

val ThemeMode.labelRes: StringResource
    get() =
        when (this) {
            ThemeMode.System -> Res.string.theme_system
            ThemeMode.Light -> Res.string.theme_light
            ThemeMode.Dark -> Res.string.theme_dark
        }

val AppColorScheme.labelRes: StringResource
    get() =
        when (this) {
            AppColorScheme.Dynamic -> Res.string.palette_dynamic
            AppColorScheme.Honey -> Res.string.palette_honey
            AppColorScheme.Indigo -> Res.string.palette_indigo
            AppColorScheme.Forest -> Res.string.palette_forest
            AppColorScheme.Rose -> Res.string.palette_rose
        }

val AppFont.labelRes: StringResource
    get() =
        when (this) {
            AppFont.System -> Res.string.font_system
            AppFont.Inter -> Res.string.font_inter
            AppFont.Lora -> Res.string.font_lora
            AppFont.JetBrainsMono -> Res.string.font_jetbrains_mono
        }
