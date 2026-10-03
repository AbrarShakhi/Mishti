package com.abrarshakhi.mishti.common.fake

import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [AppPreferences]; callers depend on the interface, not on DataStore. */
class FakeAppPreferences(
    initialInference: InferenceSettings = InferenceSettings(),
) : AppPreferences {

    private val onboarding = MutableStateFlow(false)
    private val theme = MutableStateFlow(ThemeSettings())
    private val selected = MutableStateFlow<String?>(null)
    private val inference = MutableStateFlow(initialInference)

    override val hasCompletedOnboarding: Flow<Boolean> = onboarding
    override val themeSettings: Flow<ThemeSettings> = theme
    override val selectedModelId: Flow<String?> = selected
    override val inferenceSettings: Flow<InferenceSettings> = inference

    override suspend fun setOnboardingCompleted(completed: Boolean) { onboarding.value = completed }
    override suspend fun setThemeMode(mode: ThemeMode) { theme.value = theme.value.copy(mode = mode) }
    override suspend fun setColorScheme(scheme: AppColorScheme) {
        theme.value = theme.value.copy(colorScheme = scheme)
    }
    override suspend fun setFont(font: AppFont) { theme.value = theme.value.copy(font = font) }
    override suspend fun setSelectedModelId(modelId: String?) { selected.value = modelId }
    override suspend fun setInferenceSettings(settings: InferenceSettings) {
        inference.value = settings
    }
}
