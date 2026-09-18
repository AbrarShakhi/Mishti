package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private data class SliderSpec(
    val title: String,
    val description: String,
    val value: Float,
    val range: ClosedFloatingPointRange<Float>,
    val display: (Float) -> String,
    val apply: (InferenceSettings, Float) -> InferenceSettings,
)

private fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> =
    first.toFloat()..last.toFloat()

private fun sliderSpecs(s: InferenceSettings): List<SliderSpec> = listOf(
    SliderSpec(
        title = "Temperature",
        description = "Lower is more focused, higher is more varied.",
        value = s.temperature,
        range = InferenceSettings.TemperatureRange,
        display = { "%.2f".format(it) },
        apply = { settings, v -> settings.copy(temperature = v) },
    ),
    SliderSpec(
        title = "Top-p",
        description = "Considers only the most likely tokens adding up to this probability.",
        value = s.topP,
        range = InferenceSettings.TopPRange,
        display = { "%.2f".format(it) },
        apply = { settings, v -> settings.copy(topP = v) },
    ),
    SliderSpec(
        title = "Top-k",
        description = "Considers at most this many candidate tokens per step.",
        value = s.topK.toFloat(),
        range = InferenceSettings.TopKRange.toFloatRange(),
        display = { it.roundToInt().toString() },
        apply = { settings, v -> settings.copy(topK = v.roundToInt()) },
    ),
    SliderSpec(
        title = "Response limit",
        description = "Maximum tokens in a single reply.",
        value = s.maxTokens.toFloat(),
        range = InferenceSettings.MaxTokensRange.toFloatRange(),
        display = { it.roundToInt().toString() },
        apply = { settings, v -> settings.copy(maxTokens = v.roundToInt()) },
    ),
    SliderSpec(
        title = "Context window",
        description = "How much conversation the model can see. Larger uses more memory and " +
            "reloads the model.",
        value = s.contextTokens.toFloat(),
        range = InferenceSettings.ContextRange.toFloatRange(),
        display = { "${it.roundToInt()} tokens" },
        apply = { settings, v -> settings.copy(contextTokens = v.roundToInt()) },
    ),
    SliderSpec(
        title = "Threads",
        description = "More is not always faster — phone cores throttle under load. " +
            "Reloads the model.",
        value = s.threads.toFloat(),
        range = InferenceSettings.ThreadsRange.toFloatRange(),
        display = { it.roundToInt().toString() },
        apply = { settings, v -> settings.copy(threads = v.roundToInt()) },
    ),
)

@Composable
fun InferenceSection(
    settings: InferenceSettings,
    onChange: (InferenceSettings) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<SliderSpec?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {

        PreInstructionField(
            value = settings.systemPrompt,
            onCommit = { onChange(settings.copy(systemPrompt = it)) },
        )

        sliderSpecs(settings).forEach { spec ->
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { editing = spec },
                supportingContent = { Text(spec.description) },
                trailingContent = {
                    Text(
                        text = spec.display(spec.value),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                contentPadding = SettingsRowPadding,
                content = { Text(spec.title) },
            )
        }

        TextButton(
            onClick = onReset,
            modifier = Modifier.padding(start = SettingsHorizontalPadding - 12.dp, top = 8.dp),
        ) {
            Text("Reset to defaults")
        }
    }

    editing?.let { spec ->
        SliderDialog(
            spec = spec,
            onConfirm = { value ->
                onChange(spec.apply(settings, value))
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderDialog(
    spec: SliderSpec,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = remember(spec.title) {
        SliderState(value = spec.value, trackRange = spec.range)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(spec.title) },
        text = {
            Column {
                Text(
                    text = spec.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = spec.display(state.value),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Slider(state = state, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.value) }) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

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

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Pre-instruction") },
        supportingText = {
            Text("Sent before every conversation. Sets the assistant's role and tone.")
        },
        minLines = 3,
        maxLines = 6,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsHorizontalPadding, vertical = 8.dp)
            .onFocusChanged { focus ->
                if (!focus.isFocused && text != value) currentOnCommit(text)
            },
    )
}

private const val PersistDebounceMillis = 350L
