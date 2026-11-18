package com.abrarshakhi.mishti.settings.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppFont
import com.abrarshakhi.mishti.core.domain.theme.ThemeMode
import com.abrarshakhi.mishti.core.domain.theme.ThemeSettings
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultPerformance
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultSampling

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
