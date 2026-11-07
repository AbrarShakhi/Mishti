package com.abrarshakhi.mishti.settings.ui

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.designsystem.theme.AppColorScheme
import com.abrarshakhi.mishti.core.designsystem.theme.AppFont
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeMode
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeSettings
import com.abrarshakhi.mishti.core.llm.data.ScriptedLlmEngine
import com.abrarshakhi.mishti.core.llm.domain.InferenceSettings
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import com.abrarshakhi.mishti.core.testing.FakeAppPreferences
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarMessage
import com.abrarshakhi.mishti.core.ui.utils.uiText
import com.abrarshakhi.mishti.settings.domain.usecase.UpdateInferenceSettingsUseCase
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.settings_save_failed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FailingPreferences(delegate: FakeAppPreferences = FakeAppPreferences()) : AppPreferences by delegate {
    override suspend fun updateInferenceSettings(transform: (InferenceSettings) -> InferenceSettings): Unit =
        throw IOException("No space left on device")
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val tuned =
        InferenceSettings(
            systemPrompt = "Be brief.",
            temperature = 1.2f,
            topK = 80,
            topP = 0.5f,
            maxTokens = 1024,
            contextTokens = 4096,
            threads = 6,
        )

    private fun viewModel(
        preferences: AppPreferences = FakeAppPreferences(tuned),
        snackBar: SnackBarDispatcher = SnackBarDispatcher(),
        isDynamicColorAvailable: Boolean = true,
    ) = SettingsViewModel(
        preferences = preferences,
        snackBar = snackBar,
        engine = ScriptedLlmEngine(loadDelayMillis = 0L, tokenDelayMillis = 0L),
        isDynamicColorAvailable = isDynamicColorAvailable,
        updateInferenceSettings = UpdateInferenceSettingsUseCase(preferences),
    )

    @Test
    fun `the state follows the saved settings`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(tuned, vm.state.value.inference)
        assertFalse(vm.state.value.isSamplingDefault)
        assertFalse(vm.state.value.isPerformanceDefault)
    }

    @Test
    fun `changing one value leaves the other settings alone`() = runTest {
        val preferences = FakeAppPreferences(tuned)
        val vm = viewModel(preferences)
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.TemperatureChanged(0.3f))
        advanceUntilIdle()

        assertEquals(tuned.copy(temperature = 0.3f), preferences.inferenceSettings.first())
    }

    @Test
    fun `changes sent before the state catches up are all kept`() = runTest {
        val preferences = FakeAppPreferences(tuned)
        val vm = viewModel(preferences)

        vm.onIntent(SettingsIntent.SystemPromptChanged("Answer in French."))
        vm.onIntent(SettingsIntent.MaxTokensChanged(256))
        vm.onIntent(SettingsIntent.ThreadsChanged(2))
        advanceUntilIdle()

        assertEquals(
            tuned.copy(systemPrompt = "Answer in French.", maxTokens = 256, threads = 2),
            preferences.inferenceSettings.first(),
        )
    }

    @Test
    fun `restoring sampling defaults keeps the pre-instruction and performance settings`() = runTest {
        val preferences = FakeAppPreferences(tuned)
        val vm = viewModel(preferences)
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.SamplingReset)
        advanceUntilIdle()

        val defaults = InferenceSettings()
        assertEquals(
            tuned.copy(
                temperature = defaults.temperature,
                topK = defaults.topK,
                topP = defaults.topP,
            ),
            preferences.inferenceSettings.first(),
        )
        assertTrue(vm.state.value.isSamplingDefault)
        assertFalse(vm.state.value.isPerformanceDefault)
    }

    @Test
    fun `restoring performance defaults keeps sampling and the pre-instruction`() = runTest {
        val preferences = FakeAppPreferences(tuned)
        val vm = viewModel(preferences)
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.PerformanceReset)
        advanceUntilIdle()

        val defaults = InferenceSettings()
        assertEquals(
            tuned.copy(contextTokens = defaults.contextTokens, threads = defaults.threads),
            preferences.inferenceSettings.first(),
        )
        assertTrue(vm.state.value.isPerformanceDefault)
        assertFalse(vm.state.value.isSamplingDefault)
    }

    @Test
    fun `theme choices are saved`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.Dark))
        vm.onIntent(SettingsIntent.ColorSchemeSelected(AppColorScheme.Rose))
        vm.onIntent(SettingsIntent.FontSelected(AppFont.Lora))
        advanceUntilIdle()

        assertEquals(
            ThemeSettings(ThemeMode.Dark, AppColorScheme.Rose, AppFont.Lora),
            vm.state.value.theme,
        )
    }

    @Test
    fun `Dynamic is offered only where the platform has wallpaper colours`() = runTest {
        assertTrue(AppColorScheme.Dynamic in viewModel().state.value.colorSchemes)
        assertFalse(
            AppColorScheme.Dynamic in
                viewModel(isDynamicColorAvailable = false).state.value.colorSchemes,
        )
    }

    @Test
    fun `a setting that cannot be saved is reported`() = runTest {
        val snackBar = SnackBarDispatcher()
        val vm = viewModel(preferences = FailingPreferences(), snackBar = snackBar)
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.TopKChanged(10))
        advanceUntilIdle()

        assertEquals(
            SnackBarMessage(uiText(Res.string.settings_save_failed), SnackbarDuration.Long),
            snackBar.messages.first(),
        )
    }

    @Test
    fun `a committed edit finishes even when its screen is closed`() = runTest {
        val saved = FakeAppPreferences()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val preferences = object : AppPreferences by saved {
            override suspend fun updateInferenceSettings(transform: (InferenceSettings) -> InferenceSettings) {
                started.complete(Unit)
                release.await()
                saved.updateInferenceSettings(transform)
            }
        }
        val vm = viewModel(preferences)
        vm.onIntent(SettingsIntent.SystemPromptChanged("Keep this edit."))
        started.await()
        vm.viewModelScope.cancel()
        release.complete(Unit)
        advanceUntilIdle()
        assertEquals("Keep this edit.", saved.inferenceSettings.first().systemPrompt)
    }

    @Test
    fun `unsupported wallpaper colors show the fallback palette as selected`() = runTest {
        val vm = viewModel(isDynamicColorAvailable = false)
        advanceUntilIdle()
        assertEquals(AppColorScheme.Honey, vm.state.value.theme.colorScheme)
    }
}
