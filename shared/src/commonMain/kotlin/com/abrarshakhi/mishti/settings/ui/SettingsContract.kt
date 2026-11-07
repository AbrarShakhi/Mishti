package com.abrarshakhi.mishti.settings.ui

import com.abrarshakhi.mishti.core.designsystem.theme.AppColorScheme
import com.abrarshakhi.mishti.core.designsystem.theme.AppFont
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeMode
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeSettings
import com.abrarshakhi.mishti.core.llm.domain.EngineState
import com.abrarshakhi.mishti.core.llm.domain.InferenceSettings
import com.abrarshakhi.mishti.core.ui.mvi.UiEffect
import com.abrarshakhi.mishti.core.ui.mvi.UiIntent
import com.abrarshakhi.mishti.core.ui.mvi.UiState
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultPerformance
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultSampling

data class SettingsUiState(
    val theme: ThemeSettings = ThemeSettings(),
    val inference: InferenceSettings = InferenceSettings(),
    val engineState: EngineState = EngineState.Idle,
    val isDynamicColorAvailable: Boolean = true,
) : UiState {
    val colorSchemes: List<AppColorScheme>
        get() =
            AppColorScheme.entries.filter {
                it != AppColorScheme.Dynamic || isDynamicColorAvailable
            }

    val isSamplingDefault: Boolean
        get() = inference.withDefaultSampling() == inference

    val isPerformanceDefault: Boolean
        get() = inference.withDefaultPerformance() == inference
}

sealed interface SettingsIntent : UiIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent

    data class ColorSchemeSelected(val scheme: AppColorScheme) : SettingsIntent

    data class FontSelected(val font: AppFont) : SettingsIntent

    data class SystemPromptChanged(val prompt: String) : SettingsIntent

    data class MaxTokensChanged(val tokens: Int) : SettingsIntent

    data class TemperatureChanged(val temperature: Float) : SettingsIntent

    data class TopPChanged(val topP: Float) : SettingsIntent

    data class TopKChanged(val topK: Int) : SettingsIntent

    data class ContextTokensChanged(val tokens: Int) : SettingsIntent

    data class ThreadsChanged(val threads: Int) : SettingsIntent

    data object SamplingReset : SettingsIntent

    data object PerformanceReset : SettingsIntent
}

sealed interface SettingsEffect : UiEffect
