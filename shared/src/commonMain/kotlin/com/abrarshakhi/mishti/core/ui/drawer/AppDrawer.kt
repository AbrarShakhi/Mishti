@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.core.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.chat.ui.RenameState
import com.abrarshakhi.mishti.chat.ui.SessionGroup
import com.abrarshakhi.mishti.chat.ui.SessionsIntent
import com.abrarshakhi.mishti.chat.ui.SessionsUiState
import com.abrarshakhi.mishti.chat.ui.groupSessionsByRecency
import com.abrarshakhi.mishti.chat.ui.labelRes
import com.abrarshakhi.mishti.chat.ui.sessionDisplayTitle
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.ui.components.AppMark
import com.abrarshakhi.mishti.core.ui.components.AppName
import com.abrarshakhi.mishti.core.ui.components.SectionHeader
import com.abrarshakhi.mishti.core.utils.currentTimeMillis
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.action_cancel
import com.abrarshakhi.mishti.shared.resources.action_delete
import com.abrarshakhi.mishti.shared.resources.drawer_conversation_actions
import com.abrarshakhi.mishti.shared.resources.drawer_delete_body
import com.abrarshakhi.mishti.shared.resources.drawer_delete_title
import com.abrarshakhi.mishti.shared.resources.drawer_empty
import com.abrarshakhi.mishti.shared.resources.drawer_new_chat
import com.abrarshakhi.mishti.shared.resources.drawer_rename
import com.abrarshakhi.mishti.shared.resources.drawer_rename_field
import com.abrarshakhi.mishti.shared.resources.drawer_rename_title
import com.abrarshakhi.mishti.shared.resources.drawer_settings
import com.abrarshakhi.mishti.shared.resources.models_title
import org.jetbrains.compose.resources.stringResource

private val MinimumVisibleScrim = 56.dp
private val FadeLength = 32.dp
private const val TopAnchorKey = "top"

@Composable
fun AppDrawer(
    state: SessionsUiState,
    groups: List<SessionGroup>,
    currentSessionId: String?,
    onIntent: (SessionsIntent) -> Unit,
    onModelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalWindowInfo.current.containerDpSize.width
    val drawerWidth = (screenWidth - MinimumVisibleScrim).coerceIn(0.dp, DrawerDefaults.MaximumDrawerWidth)
    val listState = rememberLazyListState()

    DismissibleDrawerSheet(
        modifier = modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        DrawerHeader()

        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(Res.string.drawer_new_chat),
                    style = MaterialTheme.typography.titleSmall,
                )
            },
            icon = {
                Icon(
                    imageVector = MaterialTheme.icons.actions.newChat.filled,
                    contentDescription = null,
                )
            },
            selected = false,
            onClick = { onIntent(SessionsIntent.NewChatClicked) },
            modifier = Modifier
                .padding(horizontal = Spacing.Medium)
                .height(48.dp),
            shape = MaterialTheme.shapes.large,
        )

        ConversationHistory(
            groups = groups,
            currentSessionId = currentSessionId,
            actionsFor = state.actionsFor,
            state = listState,
            onSessionClick = { onIntent(SessionsIntent.SessionSelected(it)) },
            onShowActions = { onIntent(SessionsIntent.SessionLongPressed(it)) },
            onActionsDismiss = { onIntent(SessionsIntent.ActionsDismissed) },
            onRename = { onIntent(SessionsIntent.RenameRequested) },
            onDelete = { onIntent(SessionsIntent.DeleteRequested) },
            modifier = Modifier.weight(1f),
        )

        DrawerFooter(
            onModelsClick = onModelsClick,
            onSettingsClick = onSettingsClick,
        )
    }

    state.renaming?.let { renaming ->
        RenameDialog(
            state = renaming,
            onTitleChange = { onIntent(SessionsIntent.RenameTitleChanged(it)) },
            onConfirm = { onIntent(SessionsIntent.RenameConfirmed) },
            onDismiss = { onIntent(SessionsIntent.RenameCancelled) },
        )
    }
    state.deleting?.let { deleting ->
        DeleteDialog(
            session = deleting,
            onConfirm = { onIntent(SessionsIntent.DeleteConfirmed(currentSessionId)) },
            onDismiss = { onIntent(SessionsIntent.DeleteCancelled) },
        )
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier.padding(
            start = Spacing.Large,
            end = Spacing.Large,
            top = Spacing.Large,
            bottom = Spacing.Medium,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppMark(size = 32.dp)
        Spacer(Modifier.width(Spacing.Medium))
        AppName()
    }
}

@Composable
private fun ConversationHistory(
    groups: List<SessionGroup>,
    currentSessionId: String?,
    actionsFor: ChatSession?,
    state: LazyListState,
    onSessionClick: (String) -> Unit,
    onShowActions: (String) -> Unit,
    onActionsDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val topFade by animateFloatAsState(
        targetValue = if (state.canScrollBackward) 1f else 0f,
        animationSpec = effects,
        label = "TopFade",
    )
    val bottomFade by animateFloatAsState(
        targetValue = if (state.canScrollForward) 1f else 0f,
        animationSpec = effects,
        label = "BottomFade",
    )

    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .fadingEdges(top = { topFade }, bottom = { bottomFade }),
        contentPadding = PaddingValues(
            start = Spacing.Medium,
            end = Spacing.Medium,
            bottom = Spacing.Large,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = TopAnchorKey) {
            Spacer(Modifier.height(Spacing.Small))
            HorizontalDivider()
        }

        if (groups.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(Res.string.drawer_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Large,
                        vertical = Spacing.Large,
                    ),
                )
            }
        }

        groups.forEachIndexed { groupIndex, group ->
            item(key = "recency-${group.recency.name}", contentType = "recency") {
                SectionHeader(
                    title = stringResource(group.recency.labelRes),
                    modifier = Modifier
                        .animateItem()
                        .padding(top = if (groupIndex == 0) Spacing.Small else Spacing.Medium),
                )
            }
            itemsIndexed(
                items = group.sessions,
                key = { _, session -> "session-${session.id}" },
                contentType = { _, _ -> "session" },
            ) { _, session ->
                ConversationItem(
                    session = session,
                    selected = session.id == currentSessionId,
                    menuExpanded = actionsFor?.id == session.id,
                    onClick = { onSessionClick(session.id) },
                    onShowActions = { onShowActions(session.id) },
                    onMenuDismiss = onActionsDismiss,
                    onRename = onRename,
                    onDelete = onDelete,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private fun Modifier.fadingEdges(top: () -> Float, bottom: () -> Float): Modifier = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithContent {
    drawContent()
    val length = FadeLength.toPx().coerceAtMost(size.height / 2)
    val topAlpha = top()
    if (topAlpha > 0f) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 1f - topAlpha), Color.Black),
                startY = 0f,
                endY = length,
            ),
            size = Size(size.width, length),
            blendMode = BlendMode.DstIn,
        )
    }
    val bottomAlpha = bottom()
    if (bottomAlpha > 0f) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Black.copy(alpha = 1f - bottomAlpha)),
                startY = size.height - length,
                endY = size.height,
            ),
            topLeft = Offset(0f, size.height - length),
            size = Size(size.width, length),
            blendMode = BlendMode.DstIn,
        )
    }
}

@Composable
private fun ConversationItem(
    session: ChatSession,
    selected: Boolean,
    menuExpanded: Boolean,
    onClick: () -> Unit,
    onShowActions: () -> Unit,
    onMenuDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val containerColor =
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Surface(
            color = containerColor,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(MaterialTheme.shapes.large)
                .semantics { this.selected = selected }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowActions()
                    },
                    onLongClickLabel = stringResource(Res.string.drawer_conversation_actions),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (selected) {
                        MaterialTheme.icons.navigation.chat.filled
                    } else {
                        MaterialTheme.icons.navigation.chat.outline
                    },
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.padding(end = 5.dp),
                )

                Text(
                    text = sessionDisplayTitle(session.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = onShowActions,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = MaterialTheme.icons.actions.more.outline,
                        contentDescription = stringResource(Res.string.drawer_conversation_actions),
                        modifier = Modifier.size(20.dp),
                        tint = contentColor,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .wrapContentWidth(Alignment.End),
        ) {
            ConversationActionsMenu(
                expanded = menuExpanded,
                onDismiss = onMenuDismiss,
                onRename = onRename,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun ConversationActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.drawer_rename)) },
            leadingIcon = {
                Icon(
                    imageVector = MaterialTheme.icons.actions.edit.outline,
                    contentDescription = null,
                )
            },
            onClick = onRename,
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(Res.string.action_delete),
                    color = colorScheme.error,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = MaterialTheme.icons.actions.delete.outline,
                    contentDescription = null,
                    tint = colorScheme.error,
                )
            },
            onClick = onDelete,
        )
    }
}

@Composable
private fun DrawerFooter(onModelsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onModelsClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier.height(48.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MaterialTheme.icons.navigation.store.filled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.Medium))
                Text(
                    text = stringResource(Res.string.models_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onSettingsClick,
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = MaterialTheme.icons.navigation.settings.outline,
                contentDescription = stringResource(Res.string.drawer_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RenameDialog(
    state: RenameState,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(MaterialTheme.icons.actions.edit.outline, contentDescription = null) },
        title = { Text(stringResource(Res.string.drawer_rename_title)) },
        text = {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text(stringResource(Res.string.drawer_rename_field)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (state.canConfirm) onConfirm() }),
                modifier = Modifier.focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.canConfirm,
            ) { Text(stringResource(Res.string.drawer_rename)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Composable
private fun DeleteDialog(session: ChatSession, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(MaterialTheme.icons.actions.delete.outline, contentDescription = null) },
        title = { Text(stringResource(Res.string.drawer_delete_title)) },
        text = {
            Text(stringResource(Res.string.drawer_delete_body, sessionDisplayTitle(session.title)))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(Res.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Preview
@Composable
private fun AppDrawerPreview() {
    val now = currentTimeMillis()
    val sessions = listOf(
        ChatSession("1", "Running a model offline", now, now),
        ChatSession("2", "Dinner ideas", now - 86_400_000L, now - 86_400_000L),
    )
    MishtiTheme {
        AppDrawer(
            state = SessionsUiState(sessions = sessions),
            groups = groupSessionsByRecency(sessions, now),
            currentSessionId = "1",
            onIntent = {},
            onModelsClick = {},
            onSettingsClick = {},
        )
    }
}
