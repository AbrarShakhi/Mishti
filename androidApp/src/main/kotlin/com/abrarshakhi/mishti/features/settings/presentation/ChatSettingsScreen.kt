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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.R
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
        title = stringResource(R.string.settings_chat),
        subtitle = stringResource(R.string.settings_chat_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = stringResource(R.string.settings_pre_instruction),
                        supporting = stringResource(R.string.settings_pre_instruction_hint),
                    ) {
                        PreInstructionField(
                            value = inference.systemPrompt,
                            onCommit = { onIntent(SettingsIntent.SystemPromptChanged(it)) },
                        )
                    }
                }
                sliderRow(
                    spec = SliderSpec(
                        title = R.string.settings_response_limit,
                        description = R.string.settings_response_limit_hint,
                        value = inference.maxTokens.toFloat(),
                        range = InferenceSettings.MaxTokensRange.toFloatRange(),
                        display = {
                            pluralStringResource(
                                R.plurals.settings_tokens,
                                it.roundToInt(),
                                it.roundToInt(),
                            )
                        },
                        intent = { SettingsIntent.MaxTokensChanged(it.roundToInt()) },
                    ),
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun PreInstructionField(value: String, onCommit: (String) -> Unit) {
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
        placeholder = { Text(stringResource(R.string.settings_pre_instruction_placeholder)) },
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
