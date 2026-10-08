package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlin.math.roundToInt
import com.abrarshakhi.mishti.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource

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
        title = stringResource(R.string.settings_performance),
        subtitle = stringResource(R.string.settings_performance_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_context_window,
                        description = R.string.settings_context_window_hint,
                        value = inference.contextTokens.toFloat(),
                        range = InferenceSettings.ContextRange.toFloatRange(),
                        display = { pluralStringResource(R.plurals.settings_tokens, it.roundToInt(), it.roundToInt()) },
                        intent = { SettingsIntent.ContextTokensChanged(it.roundToInt()) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_threads,
                        description = R.string.settings_threads_hint,
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
                            R.string.settings_performance_defaults,
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

@Preview(showBackground = true)
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
