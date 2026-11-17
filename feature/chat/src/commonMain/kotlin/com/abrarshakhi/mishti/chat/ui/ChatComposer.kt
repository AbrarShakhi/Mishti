package com.abrarshakhi.mishti.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.feature.chat.resources.chat_action_menu
import com.abrarshakhi.mishti.feature.chat.resources.chat_action_voice
import com.abrarshakhi.mishti.feature.chat.resources.chat_composer_placeholder
import com.abrarshakhi.mishti.feature.chat.resources.chat_no_model_selected
import com.abrarshakhi.mishti.feature.chat.resources.chat_send
import com.abrarshakhi.mishti.feature.chat.resources.chat_stop
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.feature.chat.resources.Res as FeatureChatRes

private const val ComposerMaxLines = 6
private val ComposerShape = RoundedCornerShape(26.dp)

@Composable
internal fun ChatComposer(
    draft: String,
    canSend: Boolean,
    canStop: Boolean,
    activeModelName: String?,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onAddClick: () -> Unit,
    onModelClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val isExpanded = isFocused || draft.isNotEmpty()
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small)
            .animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
        shape = ComposerShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                verticalAlignment = if (isExpanded) Alignment.Top else Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                ) {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = MaterialTheme.icons.actions.moreRounded.filled,
                            contentDescription = stringResource(FeatureChatRes.string.chat_action_menu),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = Spacing.Small,
                            vertical = if (isExpanded) Spacing.Small else Spacing.ExtraSmall,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draft.isEmpty()) {
                        Text(
                            text = stringResource(FeatureChatRes.string.chat_composer_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) ComposerMaxLines else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = !isExpanded,
                        maxLines = if (isExpanded) ComposerMaxLines else 1,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    )
                }

                // In unfocused state, voice button is on line 1
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                ) {
                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = MaterialTheme.icons.navigation.chat.filled,
                            contentDescription = stringResource(FeatureChatRes.string.chat_action_voice),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Second row: Only shown when focused / expanded
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                        fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) +
                        fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.ExtraSmall, bottom = Spacing.ExtraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = MaterialTheme.icons.actions.moreRounded.filled,
                            contentDescription = stringResource(FeatureChatRes.string.chat_action_menu),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    ModelSelectorPill(
                        modelName = activeModelName,
                        onClick = onModelClick,
                        modifier = Modifier.padding(start = Spacing.ExtraSmall),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = MaterialTheme.icons.actions.microphone.filled,
                            contentDescription = stringResource(FeatureChatRes.string.chat_action_voice),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    SendButton(
                        canSend = canSend,
                        canStop = canStop,
                        onSend = onSend,
                        onStop = onStop,
                        modifier = Modifier.padding(start = Spacing.ExtraSmall),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelSelectorPill(
    modelName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.height(32.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacing.Medium)
                .widthIn(max = 180.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
        ) {
            Text(
                text = modelName ?: stringResource(FeatureChatRes.string.chat_no_model_selected),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = MaterialTheme.icons.navigation.foreword.filled,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
        modifier = modifier,
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
                    contentDescription = stringResource(FeatureChatRes.string.chat_stop),
                )
            } else {
                Icon(
                    imageVector = MaterialTheme.icons.actions.send.filled,
                    contentDescription = stringResource(FeatureChatRes.string.chat_send),
                )
            }
        }
    }
}
