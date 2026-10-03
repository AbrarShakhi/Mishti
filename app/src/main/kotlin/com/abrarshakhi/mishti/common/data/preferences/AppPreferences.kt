package com.abrarshakhi.mishti.common.data.preferences

import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.Flow


interface AppPreferences {

    val hasCompletedOnboarding: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    val themeSettings: Flow<ThemeSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setColorScheme(scheme: AppColorScheme)

    suspend fun setFont(font: AppFont)

    val selectedModelId: Flow<String?>

    suspend fun setSelectedModelId(modelId: String?)

    val inferenceSettings: Flow<InferenceSettings>

    suspend fun setInferenceSettings(settings: InferenceSettings)
}
