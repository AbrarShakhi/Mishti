package com.abrarshakhi.mishti.settings.domain

import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.core.testing.FakeAppPreferences
import com.abrarshakhi.mishti.settings.domain.usecase.UpdateInferenceSettingsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateInferenceSettingsUseCaseTest {
    @Test
    fun `invalid numeric values are constrained before they reach storage`() = runTest {
        val preferences = FakeAppPreferences()
        val update = UpdateInferenceSettingsUseCase(preferences)
        update {
            copy(
                temperature = Float.NaN,
                topP = Float.POSITIVE_INFINITY,
                topK = -1,
                maxTokens = Int.MAX_VALUE,
                contextTokens = 0,
                threads = 100,
            )
        }
        val stored = preferences.inferenceSettings.first()
        assertEquals(InferenceSettings().temperature, stored.temperature)
        assertEquals(InferenceSettings().topP, stored.topP)
        assertEquals(InferenceSettings.TopKRange.first, stored.topK)
        assertEquals(InferenceSettings.MaxTokensRange.last, stored.maxTokens)
        assertEquals(InferenceSettings.ContextRange.first, stored.contextTokens)
        assertEquals(InferenceSettings.ThreadsRange.last, stored.threads)
    }

    @Test
    fun `editing one setting retains previously saved edits`() = runTest {
        val preferences = FakeAppPreferences()
        val update = UpdateInferenceSettingsUseCase(preferences)
        update { copy(systemPrompt = "Be concise.") }
        update { copy(temperature = 0.5f) }
        update { copy(threads = 2) }
        assertEquals(
            InferenceSettings(systemPrompt = "Be concise.", temperature = 0.5f, threads = 2),
            preferences.inferenceSettings.first(),
        )
    }
}
