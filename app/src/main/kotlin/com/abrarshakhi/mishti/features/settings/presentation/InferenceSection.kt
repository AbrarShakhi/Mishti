package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val PersistDebounceMillis = 350L

internal data class SliderSpec(
    val title: String,
    val description: String,
    val value: Float,
    val range: ClosedFloatingPointRange<Float>,
    val display: (Float) -> String,
    val apply: (InferenceSettings, Float) -> InferenceSettings,
    val steps: Int = 0,
)

private fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> =
    first.toFloat()..last.toFloat()

private fun decimal(value: Float) = "%.2f".format(value)

private fun whole(value: Float) = value.roundToInt().toString()

internal fun SettingsGroupScope.responseRows(
    settings: InferenceSettings,
    onChange: (InferenceSettings) -> Unit,
) {
    row { shapes ->
        SegmentedCard(shapes) {
            PreInstructionField(
                value = settings.systemPrompt,
                onCommit = { onChange(settings.copy(systemPrompt = it)) },
            )
        }
    }
    sliderRow(
        spec = SliderSpec(
            title = "Response limit",
            description = "The most tokens a single reply may use.",
            value = settings.maxTokens.toFloat(),
            range = InferenceSettings.MaxTokensRange.toFloatRange(),
            display = { "${whole(it)} tokens" },
            apply = { s, v -> s.copy(maxTokens = v.roundToInt()) },
        ),
        settings = settings,
        onChange = onChange,
    )
}

internal fun SettingsGroupScope.samplingRows(
    settings: InferenceSettings,
    onChange: (InferenceSettings) -> Unit,
) {
    listOf(
        SliderSpec(
            title = "Temperature",
            description = "Lower is more focused, higher is more varied.",
            value = settings.temperature,
            range = InferenceSettings.TemperatureRange,
            display = ::decimal,
            apply = { s, v -> s.copy(temperature = v) },
        ),
        SliderSpec(
            title = "Top-p",
            description = "Considers only the likeliest tokens that add up to this probability.",
            value = settings.topP,
            range = InferenceSettings.TopPRange,
            display = ::decimal,
            apply = { s, v -> s.copy(topP = v) },
        ),
        SliderSpec(
            title = "Top-k",
            description = "Considers at most this many candidate tokens at each step.",
            value = settings.topK.toFloat(),
            range = InferenceSettings.TopKRange.toFloatRange(),
            display = ::whole,
            apply = { s, v -> s.copy(topK = v.roundToInt()) },
        ),
    ).forEach { sliderRow(it, settings, onChange) }
}

internal fun SettingsGroupScope.performanceRows(
    settings: InferenceSettings,
    onChange: (InferenceSettings) -> Unit,
) {
    listOf(
        SliderSpec(
            title = "Context window",
            description = "How much of the conversation the model can see. Larger uses more " +
                "memory, and changing it reloads the model.",
            value = settings.contextTokens.toFloat(),
            range = InferenceSettings.ContextRange.toFloatRange(),
            display = { "${whole(it)} tokens" },
            apply = { s, v -> s.copy(contextTokens = v.roundToInt()) },
        ),
        SliderSpec(
            title = "Threads",
            description = "More is not always faster, as phone cores throttle under load. " +
                "Changing it reloads the model.",
            value = settings.threads.toFloat(),
            range = InferenceSettings.ThreadsRange.toFloatRange(),
            display = ::whole,
            apply = { s, v -> s.copy(threads = v.roundToInt()) },
            steps = InferenceSettings.ThreadsRange.last - InferenceSettings.ThreadsRange.first - 1,
        ),
    ).forEach { sliderRow(it, settings, onChange) }
}

private fun SettingsGroupScope.sliderRow(
    spec: SliderSpec,
    settings: InferenceSettings,
    onChange: (InferenceSettings) -> Unit,
) {
    row { shapes ->
        SliderSetting(
            spec = spec,
            shapes = shapes,
            onCommit = { onChange(spec.apply(settings, it)) },
        )
    }
}

@Composable
private fun SliderSetting(
    spec: SliderSpec,
    shapes: ListItemShapes,
    onCommit: (Float) -> Unit,
) {
    // Dragging only moves this row. The setting is written once the drag ends, so a change to
    // the context window or threads reloads the model once rather than on every frame.
    val sliderState = remember(spec.value, spec.steps, spec.range) {
        SliderState(value = spec.value, steps = spec.steps, trackRange = spec.range)
    }

    SegmentedCard(shapes) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingTitle(title = spec.title, modifier = Modifier.weight(1f))
            Text(
                text = spec.display(sliderState.value),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = spec.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.Small))
        Slider(
            state = sliderState,
            onValueChange = { sliderState.value = it },
            onValueChangeFinished = { onCommit(sliderState.value) },
        )
    }
}

/**
 * The system prompt. It is written after a short pause in typing, and immediately when the
 * field loses focus, rather than on every keystroke.
 */
@Composable
private fun PreInstructionField(
    value: String,
    onCommit: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf(value) }
    val currentOnCommit by rememberUpdatedState(onCommit)

    LaunchedEffect(value) {
        if (value != text) text = value
    }

    LaunchedEffect(text) {
        if (text != value) {
            delay(PersistDebounceMillis.milliseconds)
            currentOnCommit(text)
        }
    }

    SettingTitle(
        title = "Pre-instruction",
        supporting = "Sent before every conversation to set the assistant's role and tone.",
    )
    Spacer(Modifier.height(Spacing.Medium))
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("e.g. You are a friendly, concise assistant.") },
        minLines = 3,
        maxLines = 6,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focus ->
                if (!focus.isFocused && text != value) currentOnCommit(text)
            },
    )
}
