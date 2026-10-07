package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlin.math.roundToInt

@Composable
fun GenerationSettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inference = state.inference
    val defaults = InferenceSettings()

    SettingsScaffold(
        title = "Generation",
        subtitle = GenerationSubtitle,
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = "Temperature",
                        description = "Lower is more focused, higher is more varied.",
                        value = inference.temperature,
                        range = InferenceSettings.TemperatureRange,
                        display = ::decimal,
                        intent = { SettingsIntent.TemperatureChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = "Top-p",
                        description = "Considers only the likeliest tokens that add up to this " +
                            "probability.",
                        value = inference.topP,
                        range = InferenceSettings.TopPRange,
                        display = ::decimal,
                        intent = { SettingsIntent.TopPChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = "Top-k",
                        description = "Considers at most this many candidate tokens at each step.",
                        value = inference.topK.toFloat(),
                        range = InferenceSettings.TopKRange.toFloatRange(),
                        display = { it.roundToInt().toString() },
                        intent = { SettingsIntent.TopKChanged(it.roundToInt()) },
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
                        defaults = "Temperature ${decimal(defaults.temperature)}, " +
                            "top-p ${decimal(defaults.topP)}, top-k ${defaults.topK}",
                        enabled = !state.isSamplingDefault,
                        onClick = { onIntent(SettingsIntent.SamplingReset) },
                    )
                }
            }
        }
    }
}

private fun decimal(value: Float) = "%.2f".format(value)

@Preview(showBackground = true)
@Composable
private fun GenerationSettingsScreenPreview() {
    MishtiTheme {
        GenerationSettingsScreen(
            state = SettingsUiState(inference = InferenceSettings(temperature = 1.1f)),
            onIntent = {},
            onBack = {},
        )
    }
}
