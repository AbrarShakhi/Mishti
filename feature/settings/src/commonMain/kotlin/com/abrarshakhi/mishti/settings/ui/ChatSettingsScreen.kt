@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.feature.settings.resources.settings_chat
import com.abrarshakhi.mishti.feature.settings.resources.settings_chat_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_pre_instruction
import com.abrarshakhi.mishti.feature.settings.resources.settings_pre_instruction_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_pre_instruction_placeholder
import com.abrarshakhi.mishti.feature.settings.resources.settings_response_limit
import com.abrarshakhi.mishti.feature.settings.resources.settings_response_limit_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_tokens
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

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
        title = stringResource(FeatureSettingsRes.string.settings_chat),
        subtitle = stringResource(FeatureSettingsRes.string.settings_chat_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = stringResource(FeatureSettingsRes.string.settings_pre_instruction),
                        supporting = stringResource(FeatureSettingsRes.string.settings_pre_instruction_hint),
                    ) {
                        PreInstructionField(
                            value = inference.systemPrompt,
                            onCommit = { onIntent(SettingsIntent.SystemPromptChanged(it)) },
                        )
                    }
                }
                sliderRow(
                    spec = SliderSpec(
                        title = FeatureSettingsRes.string.settings_response_limit,
                        description = FeatureSettingsRes.string.settings_response_limit_hint,
                        value = inference.maxTokens.toFloat(),
                        range = InferenceSettings.MaxTokensRange.toFloatRange(),
                        display = {
                            pluralStringResource(
                                FeatureSettingsRes.plurals.settings_tokens,
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
    var editing by remember { mutableStateOf(false) }
    val savedValue by rememberUpdatedState(value)
    val currentOnCommit by rememberUpdatedState(onCommit)

    LaunchedEffect(value) {
        if (!editing && value != text) text = value
    }

    LaunchedEffect(text, editing) {
        if (editing && text != savedValue) {
            delay(PersistDebounceMillis.milliseconds)
            if (text != savedValue) currentOnCommit(text)
        }
    }

    DisposableEffect(Unit) {
        onDispose { if (text != savedValue) currentOnCommit(text) }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(stringResource(FeatureSettingsRes.string.settings_pre_instruction_placeholder)) },
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
                editing = focus.isFocused
                if (!focus.isFocused && text != savedValue) currentOnCommit(text)
            },
    )
}

@Preview
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
