package com.abrarshakhi.mishti.settings.ui

import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppFont
import com.abrarshakhi.mishti.core.domain.theme.ThemeMode
import com.abrarshakhi.mishti.feature.settings.resources.font_inter
import com.abrarshakhi.mishti.feature.settings.resources.font_jetbrains_mono
import com.abrarshakhi.mishti.feature.settings.resources.font_lora
import com.abrarshakhi.mishti.feature.settings.resources.font_system
import com.abrarshakhi.mishti.feature.settings.resources.palette_dynamic
import com.abrarshakhi.mishti.feature.settings.resources.palette_forest
import com.abrarshakhi.mishti.feature.settings.resources.palette_honey
import com.abrarshakhi.mishti.feature.settings.resources.palette_indigo
import com.abrarshakhi.mishti.feature.settings.resources.palette_rose
import com.abrarshakhi.mishti.feature.settings.resources.theme_dark
import com.abrarshakhi.mishti.feature.settings.resources.theme_light
import com.abrarshakhi.mishti.feature.settings.resources.theme_system
import org.jetbrains.compose.resources.StringResource
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

val ThemeMode.labelRes: StringResource
    get() =
        when (this) {
            ThemeMode.System -> FeatureSettingsRes.string.theme_system
            ThemeMode.Light -> FeatureSettingsRes.string.theme_light
            ThemeMode.Dark -> FeatureSettingsRes.string.theme_dark
        }

val AppColorScheme.labelRes: StringResource
    get() =
        when (this) {
            AppColorScheme.Dynamic -> FeatureSettingsRes.string.palette_dynamic
            AppColorScheme.Honey -> FeatureSettingsRes.string.palette_honey
            AppColorScheme.Indigo -> FeatureSettingsRes.string.palette_indigo
            AppColorScheme.Forest -> FeatureSettingsRes.string.palette_forest
            AppColorScheme.Rose -> FeatureSettingsRes.string.palette_rose
        }

val AppFont.labelRes: StringResource
    get() =
        when (this) {
            AppFont.System -> FeatureSettingsRes.string.font_system
            AppFont.Inter -> FeatureSettingsRes.string.font_inter
            AppFont.Lora -> FeatureSettingsRes.string.font_lora
            AppFont.JetBrainsMono -> FeatureSettingsRes.string.font_jetbrains_mono
        }
