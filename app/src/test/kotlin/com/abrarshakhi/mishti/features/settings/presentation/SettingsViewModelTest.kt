package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.material3.SnackbarDuration
import com.abrarshakhi.mishti.common.MainDispatcherRule
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.fake.FakeAppPreferences
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.ScriptedLlmEngine
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarMessage
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private class FailingPreferences(
    delegate: FakeAppPreferences = FakeAppPreferences(),
) : AppPreferences by delegate {
    override suspend fun updateInferenceSettings(
        transform: (InferenceSettings) -> InferenceSettings,
    ) {
        throw IOException("No space left on device")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tuned = InferenceSettings(
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
        snackbar: SnackbarDispatcher = SnackbarDispatcher(),
        isDynamicColorAvailable: Boolean = true,
    ) = SettingsViewModel(
        preferences = preferences,
        snackbar = snackbar,
        engine = ScriptedLlmEngine(loadDelayMillis = 0L, tokenDelayMillis = 0L),
        isDynamicColorAvailable = isDynamicColorAvailable,
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
    fun `restoring sampling defaults keeps the pre-instruction and performance settings`() =
        runTest {
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
        val snackbar = SnackbarDispatcher()
        val vm = viewModel(preferences = FailingPreferences(), snackbar = snackbar)
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.TopKChanged(10))
        advanceUntilIdle()

        assertEquals(
            SnackbarMessage("Could not save that setting.", SnackbarDuration.Long),
            snackbar.messages.first(),
        )
    }
}
