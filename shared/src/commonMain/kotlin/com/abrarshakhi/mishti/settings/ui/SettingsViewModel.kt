package com.abrarshakhi.mishti.settings.ui

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.designsystem.theme.AppColorScheme
import com.abrarshakhi.mishti.core.llm.domain.InferenceSettings
import com.abrarshakhi.mishti.core.llm.domain.LlmEngine
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import com.abrarshakhi.mishti.core.ui.mvi.MviViewModel
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import com.abrarshakhi.mishti.settings.domain.usecase.UpdateInferenceSettingsUseCase
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultPerformance
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultSampling
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.settings_save_failed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val snackBar: SnackBarDispatcher,
    engine: LlmEngine,
    private val isDynamicColorAvailable: Boolean,
    private val updateInferenceSettings: UpdateInferenceSettingsUseCase,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(
    SettingsUiState(isDynamicColorAvailable = isDynamicColorAvailable),
) {
    init {
        viewModelScope.launch {
            preferences.themeSettings.collect { theme ->
                val supportedTheme = if (!isDynamicColorAvailable && theme.colorScheme == AppColorScheme.Dynamic) {
                    theme.copy(colorScheme = AppColorScheme.Honey)
                } else {
                    theme
                }
                updateState { copy(theme = supportedTheme) }
            }
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
            is SettingsIntent.ThemeModeSelected -> {
                write { setThemeMode(intent.mode) }
            }

            is SettingsIntent.ColorSchemeSelected -> {
                write { setColorScheme(intent.scheme) }
            }

            is SettingsIntent.FontSelected -> {
                write { setFont(intent.font) }
            }

            is SettingsIntent.SystemPromptChanged -> {
                updateInference {
                    copy(systemPrompt = intent.prompt)
                }
            }

            is SettingsIntent.MaxTokensChanged -> {
                updateInference { copy(maxTokens = intent.tokens) }
            }

            is SettingsIntent.TemperatureChanged -> {
                updateInference {
                    copy(temperature = intent.temperature)
                }
            }

            is SettingsIntent.TopPChanged -> {
                updateInference { copy(topP = intent.topP) }
            }

            is SettingsIntent.TopKChanged -> {
                updateInference { copy(topK = intent.topK) }
            }

            is SettingsIntent.ContextTokensChanged -> {
                updateInference {
                    copy(contextTokens = intent.tokens)
                }
            }

            is SettingsIntent.ThreadsChanged -> {
                updateInference { copy(threads = intent.threads) }
            }

            SettingsIntent.SamplingReset -> {
                updateInference { withDefaultSampling() }
            }

            SettingsIntent.PerformanceReset -> {
                updateInference { withDefaultPerformance() }
            }
        }
    }

    private fun updateInference(transform: InferenceSettings.() -> InferenceSettings) {
        write { this@SettingsViewModel.updateInferenceSettings(transform) }
    }

    private fun write(block: suspend AppPreferences.() -> Unit) {
        viewModelScope.launch {
            try {
                withContext(NonCancellable) { preferences.block() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackBar.showError(Res.string.settings_save_failed)
            }
        }
    }
}
