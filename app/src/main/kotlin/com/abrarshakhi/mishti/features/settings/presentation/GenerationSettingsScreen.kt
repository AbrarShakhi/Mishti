package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlin.math.roundToInt
import com.abrarshakhi.mishti.R
import androidx.compose.ui.res.stringResource

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
        title = stringResource(R.string.settings_generation),
        subtitle = stringResource(R.string.settings_generation_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_temperature,
                        description = R.string.settings_temperature_hint,
                        value = inference.temperature,
                        range = InferenceSettings.TemperatureRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TemperatureChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_top_p,
                        description = R.string.settings_top_p_hint,
                        value = inference.topP,
                        range = InferenceSettings.TopPRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TopPChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_top_k,
                        description = R.string.settings_top_k_hint,
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
                        defaults = stringResource(
                            R.string.settings_sampling_defaults,
                            decimal(defaults.temperature),
                            decimal(defaults.topP),
                            defaults.topK,
                        ),
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
