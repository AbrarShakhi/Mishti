package com.abrarshakhi.mishti.features.settings.presentation

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences,
    isDynamicColorAvailable: Boolean,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(
    SettingsUiState(isDynamicColorAvailable = isDynamicColorAvailable)
) {

    init {
        viewModelScope.launch {
            preferences.themeSettings.collect { theme -> updateState { copy(theme = theme) } }
        }
        viewModelScope.launch {
            preferences.inferenceSettings.collect { updateState { copy(inference = it) } }
        }
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> write { setThemeMode(intent.mode) }
            is SettingsIntent.ColorSchemeSelected -> write { setColorScheme(intent.scheme) }
            is SettingsIntent.FontSelected -> write { setFont(intent.font) }
            is SettingsIntent.InferenceChanged -> write { setInferenceSettings(intent.settings) }
            SettingsIntent.InferenceReset -> write { setInferenceSettings(InferenceSettings()) }
        }
    }

    private fun write(block: suspend AppPreferences.() -> Unit) {
        viewModelScope.launch { runCatching { preferences.block() } }
    }
}
