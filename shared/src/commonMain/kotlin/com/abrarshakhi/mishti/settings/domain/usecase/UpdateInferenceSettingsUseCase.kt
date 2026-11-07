package com.abrarshakhi.mishti.settings.domain.usecase

import com.abrarshakhi.mishti.core.llm.domain.InferenceSettings
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences

class UpdateInferenceSettingsUseCase(private val preferences: AppPreferences) {
    suspend operator fun invoke(transform: InferenceSettings.() -> InferenceSettings) {
        preferences.updateInferenceSettings { current -> transform(current).validated() }
    }
}

private fun InferenceSettings.validated(): InferenceSettings {
    val defaults = InferenceSettings()
    return copy(
        temperature = temperature.validValue(InferenceSettings.TemperatureRange, defaults.temperature),
        topP = topP.validValue(InferenceSettings.TopPRange, defaults.topP),
        topK = topK.coerceIn(InferenceSettings.TopKRange),
        maxTokens = maxTokens.coerceIn(InferenceSettings.MaxTokensRange),
        contextTokens = contextTokens.coerceIn(InferenceSettings.ContextRange),
        threads = threads.coerceIn(InferenceSettings.ThreadsRange),
    )
}

fun InferenceSettings.withDefaultSampling(): InferenceSettings {
    val defaults = InferenceSettings()
    return copy(temperature = defaults.temperature, topP = defaults.topP, topK = defaults.topK)
}

fun InferenceSettings.withDefaultPerformance(): InferenceSettings {
    val defaults = InferenceSettings()
    return copy(contextTokens = defaults.contextTokens, threads = defaults.threads)
}

private fun Float.validValue(range: ClosedFloatingPointRange<Float>, fallback: Float): Float =
    if (isFinite()) coerceIn(range) else fallback
