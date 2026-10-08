package com.abrarshakhi.mishti.features.settings.presentation

import com.abrarshakhi.mishti.R
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val snackbar: SnackbarDispatcher,
    engine: LlmEngine,
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
        viewModelScope.launch {
            engine.state.collect { updateState { copy(engineState = it) } }
        }
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> write { setThemeMode(intent.mode) }
            is SettingsIntent.ColorSchemeSelected -> write { setColorScheme(intent.scheme) }
            is SettingsIntent.FontSelected -> write { setFont(intent.font) }
            is SettingsIntent.SystemPromptChanged -> updateInference {
                copy(systemPrompt = intent.prompt)
            }
            is SettingsIntent.MaxTokensChanged -> updateInference { copy(maxTokens = intent.tokens) }
            is SettingsIntent.TemperatureChanged -> updateInference {
                copy(temperature = intent.temperature)
            }
            is SettingsIntent.TopPChanged -> updateInference { copy(topP = intent.topP) }
            is SettingsIntent.TopKChanged -> updateInference { copy(topK = intent.topK) }
            is SettingsIntent.ContextTokensChanged -> updateInference {
                copy(contextTokens = intent.tokens)
            }
            is SettingsIntent.ThreadsChanged -> updateInference { copy(threads = intent.threads) }
            SettingsIntent.SamplingReset -> updateInference { withDefaultSampling() }
            SettingsIntent.PerformanceReset -> updateInference { withDefaultPerformance() }
        }
    }

    private fun updateInference(transform: InferenceSettings.() -> InferenceSettings) {
        write { updateInferenceSettings(transform) }
    }

    private fun write(block: suspend AppPreferences.() -> Unit) {
        viewModelScope.launch {
            try {
                preferences.block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackbar.showError(R.string.settings_save_failed)
            }
        }
    }
}
