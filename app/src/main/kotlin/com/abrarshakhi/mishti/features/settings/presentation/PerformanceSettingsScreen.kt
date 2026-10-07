package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlin.math.roundToInt

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
        title = "Performance",
        subtitle = PerformanceSubtitle,
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                sliderRow(
                    spec = SliderSpec(
                        title = "Context window",
                        description = "How much of the conversation the model can see. Larger " +
                            "uses more memory, and changing it reloads the model.",
                        value = inference.contextTokens.toFloat(),
                        range = InferenceSettings.ContextRange.toFloatRange(),
                        display = { "${it.roundToInt()} tokens" },
                        intent = { SettingsIntent.ContextTokensChanged(it.roundToInt()) },
                    ),
                    onIntent = onIntent,
                )
                sliderRow(
                    spec = SliderSpec(
                        title = "Threads",
                        description = "More is not always faster, as phone cores throttle " +
                            "under load. Changing it reloads the model.",
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
                        defaults = "${defaults.contextTokens}-token context, " +
                            "${defaults.threads} threads",
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
