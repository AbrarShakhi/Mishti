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
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_sampling_defaults
import com.abrarshakhi.mishti.feature.settings.resources.settings_temperature
import com.abrarshakhi.mishti.feature.settings.resources.settings_temperature_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_k
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_k_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_p
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_p_hint
import com.abrarshakhi.mishti.settings.ui.components.RestoreDefaultsRow
import com.abrarshakhi.mishti.settings.ui.components.SettingsGroup
import com.abrarshakhi.mishti.settings.ui.components.SettingsScaffold
import com.abrarshakhi.mishti.settings.ui.components.SliderSpec
import com.abrarshakhi.mishti.settings.ui.components.sliderRow
import com.abrarshakhi.mishti.settings.ui.utils.decimal
import com.abrarshakhi.mishti.settings.ui.utils.toFloatRange
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.SettingsIntent
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.SettingsUiState
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

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
        title = stringResource(FeatureSettingsRes.string.settings_generation),
        subtitle = stringResource(FeatureSettingsRes.string.settings_generation_subtitle),
        onNavigate = onBack,
        isRootSetting = false,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_temperature,
                        description = FeatureSettingsRes.string.settings_temperature_hint,
                        value = inference.temperature,
                        range = InferenceSettings.TemperatureRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TemperatureChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_top_p,
                        description = FeatureSettingsRes.string.settings_top_p_hint,
                        value = inference.topP,
                        range = InferenceSettings.TopPRange,
                        display = { decimal(it) },
                        intent = { SettingsIntent.TopPChanged(it) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_top_k,
                        description = FeatureSettingsRes.string.settings_top_k_hint,
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
                            FeatureSettingsRes.string.settings_sampling_defaults,
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
