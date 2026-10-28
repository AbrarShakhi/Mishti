package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.common.ui.theme.icons

private const val ComposerMaxLines = 6

@Composable
internal fun ChatComposer(
    draft: String,
    canSend: Boolean,
    canStop: Boolean,
    thinkingSupported: Boolean,
    thinkingEnabled: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onThinkingToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column {
            TextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.padding(end = Spacing.Small),
                placeholder = { Text(stringResource(R.string.chat_composer_placeholder)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = ComposerMaxLines,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )

            Row(
                modifier = Modifier.padding(end = Spacing.Small),
                verticalAlignment = Alignment.Bottom,
            ) {
                AnimatedVisibility(
                    visible = thinkingSupported,
                    enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                        fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) +
                        fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                ) {
                    ThinkingChip(
                        selected = thinkingEnabled,
                        onClick = onThinkingToggle,
                        modifier = Modifier.padding(start = Spacing.Medium),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                SendButton(
                    canSend = canSend,
                    canStop = canStop,
                    onSend = onSend,
                    onStop = onStop,
                    modifier = Modifier.padding(bottom = Spacing.Small),
                )
            }
        }
    }
}

@Composable
private fun ThinkingChip(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(R.string.chat_think)) },
        leadingIcon = {
            Icon(
                imageVector = if (selected) {
                    MaterialTheme.icons.actions.thinking.filled
                } else {
                    MaterialTheme.icons.actions.thinking.outline
                },
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        },
        shape = CircleShape,
        modifier = modifier,
    )
}

@Composable
private fun SendButton(
    canSend: Boolean,
    canStop: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = if (canStop) onStop else onSend,
        shapes = IconButtonDefaults.shapes(),
        modifier = modifier.size(
            IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide),
        ),
        enabled = canStop || canSend,
    ) {
        AnimatedContent(
            targetState = canStop,
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "SendButtonIcon",
        ) { stopping ->
            if (stopping) {
                Icon(
                    imageVector = MaterialTheme.icons.actions.pause.filled,
                    contentDescription = stringResource(R.string.chat_stop),
                )
            } else {
                Icon(
                    imageVector = MaterialTheme.icons.actions.send.filled,
                    contentDescription = stringResource(R.string.chat_send),
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatComposerPreview() {
    ChatComposer(
        draft = "This is a draft message",
        canSend = true,
        canStop = false,
        thinkingSupported = true,
        thinkingEnabled = true,
        onDraftChange = {},
        onSend = {},
        onStop = {},
        onThinkingToggle = {},
    )
}
