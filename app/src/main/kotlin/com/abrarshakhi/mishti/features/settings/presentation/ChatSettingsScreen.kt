package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val PersistDebounceMillis = 350L

@Composable
fun ChatSettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inference = state.inference

    SettingsScaffold(
        title = "Chat",
        subtitle = ChatSubtitle,
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = "Pre-instruction",
                        supporting = "Sent before every conversation to set the assistant's " +
                            "role and tone.",
                    ) {
                        PreInstructionField(
                            value = inference.systemPrompt,
                            onCommit = { onIntent(SettingsIntent.SystemPromptChanged(it)) },
                        )
                    }
                }
                sliderRow(
                    spec = SliderSpec(
                        title = "Response limit",
                        description = "The most tokens a single reply may use.",
                        value = inference.maxTokens.toFloat(),
                        range = InferenceSettings.MaxTokensRange.toFloatRange(),
                        display = { "${it.roundToInt()} tokens" },
                        intent = { SettingsIntent.MaxTokensChanged(it.roundToInt()) },
                    ),
                    onIntent = onIntent,
                )
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
private fun ChatSettingsScreenPreview() {
    MishtiTheme {
        ChatSettingsScreen(
            state = SettingsUiState(),
            onIntent = {},
            onBack = {},
        )
    }
}
