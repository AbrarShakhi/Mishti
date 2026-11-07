@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.feature.settings.resources.settings_context_window
import com.abrarshakhi.mishti.feature.settings.resources.settings_context_window_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_performance
import com.abrarshakhi.mishti.feature.settings.resources.settings_performance_defaults
import com.abrarshakhi.mishti.feature.settings.resources.settings_performance_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_threads
import com.abrarshakhi.mishti.feature.settings.resources.settings_threads_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_tokens
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

@Composable
fun PerformanceSettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inference = state.inference
    val defaults = InferenceSettings()
    val threads = InferenceSettings.ThreadsRange

    SettingsScaffold(
        title = stringResource(FeatureSettingsRes.string.settings_performance),
        subtitle = stringResource(FeatureSettingsRes.string.settings_performance_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_context_window,
                        description = FeatureSettingsRes.string.settings_context_window_hint,
                        value = inference.contextTokens.toFloat(),
                        range = InferenceSettings.ContextRange.toFloatRange(),
                        display = {
                            pluralStringResource(
                                FeatureSettingsRes.plurals.settings_tokens,
                                it.roundToInt(),
                                it.roundToInt(),
                            )
                        },
                        intent = { SettingsIntent.ContextTokensChanged(it.roundToInt()) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_threads,
                        description = FeatureSettingsRes.string.settings_threads_hint,
                        value = inference.threads.toFloat(),
                        range = threads.toFloatRange(),
                        display = { it.roundToInt().toString() },
                        intent = { SettingsIntent.ThreadsChanged(it.roundToInt()) },
                        steps = threads.last - threads.first - 1,
                    ),
                    onIntent = onIntent,
                )
            }
        }
        item {
            SettingsGroup {
                row { shapes ->
                    RestoreDefaultsRow(
                        shapes = shapes,
                        defaults = stringResource(
                            FeatureSettingsRes.string.settings_performance_defaults,
                            defaults.contextTokens,
                            defaults.threads,
                        ),
                        enabled = !state.isPerformanceDefault,
                        onClick = { onIntent(SettingsIntent.PerformanceReset) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PerformanceSettingsScreenPreview() {
    MishtiTheme {
        PerformanceSettingsScreen(
            state = SettingsUiState(),
            onIntent = {},
            onBack = {},
        )
    }
}
