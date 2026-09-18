package com.abrarshakhi.mishti.features.settings.presentation

import com.abrarshakhi.mishti.common.mvi.UiEffect
import com.abrarshakhi.mishti.common.mvi.UiIntent
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.mvi.UiState
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings

data class SettingsUiState(
    val theme: ThemeSettings = ThemeSettings(),
    val inference: InferenceSettings = InferenceSettings(),
    val isDynamicColorAvailable: Boolean = true,
) : UiState {
    val colorSchemes: List<AppColorScheme>
        get() = AppColorScheme.entries.filter {
            it != AppColorScheme.Dynamic || isDynamicColorAvailable
        }
}

sealed interface SettingsIntent : UiIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent

    data class ColorSchemeSelected(val scheme: AppColorScheme) : SettingsIntent

    data class FontSelected(val font: AppFont) : SettingsIntent

    data class InferenceChanged(val settings: InferenceSettings) : SettingsIntent

    data object InferenceReset : SettingsIntent
}

sealed interface SettingsEffect : UiEffect
