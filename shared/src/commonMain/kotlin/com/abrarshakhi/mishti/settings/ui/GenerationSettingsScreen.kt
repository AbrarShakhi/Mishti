@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.llm.domain.InferenceSettings
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.settings_generation
import com.abrarshakhi.mishti.shared.resources.settings_generation_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_sampling_defaults
import com.abrarshakhi.mishti.shared.resources.settings_temperature
import com.abrarshakhi.mishti.shared.resources.settings_temperature_hint
import com.abrarshakhi.mishti.shared.resources.settings_top_k
import com.abrarshakhi.mishti.shared.resources.settings_top_k_hint
import com.abrarshakhi.mishti.shared.resources.settings_top_p
import com.abrarshakhi.mishti.shared.resources.settings_top_p_hint
import org.jetbrains.compose.resources.stringResource
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
        title = stringResource(Res.string.settings_generation),
        subtitle = stringResource(Res.string.settings_generation_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = Res.string.settings_temperature,
                        description = Res.string.settings_temperature_hint,
                        value = inference.temperature,
                        range = InferenceSettings.TemperatureRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TemperatureChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = Res.string.settings_top_p,
                        description = Res.string.settings_top_p_hint,
                        value = inference.topP,
                        range = InferenceSettings.TopPRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TopPChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = Res.string.settings_top_k,
                        description = Res.string.settings_top_k_hint,
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
                            Res.string.settings_sampling_defaults,
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

private fun decimal(value: Float): String {
    val hundredths = (value * 100).roundToInt()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}"
}

@Preview
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
