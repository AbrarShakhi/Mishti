package com.abrarshakhi.mishti.common.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.AppName
import com.abrarshakhi.mishti.common.ui.components.SectionHeader
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.common.ui.theme.icons
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.presentation.RenameState
import com.abrarshakhi.mishti.features.chat.presentation.SessionGroup
import com.abrarshakhi.mishti.features.chat.presentation.groupSessionsByRecency
import com.abrarshakhi.mishti.features.chat.presentation.labelRes
import com.abrarshakhi.mishti.features.chat.presentation.sessionDisplayTitle
import java.time.ZoneId
import kotlin.math.min

private val MinimumVisibleScrim = 56.dp
private val FadeLength = 32.dp
private const val TopAnchorKey = "top"

@Composable
fun AppDrawer(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    actionsFor: ChatSession?,
    renaming: RenameState?,
    deleting: ChatSession?,
    onSessionClick: (String) -> Unit,
    onSessionLongPress: (String) -> Unit,
    onActionsDismiss: () -> Unit,
    onRenameRequest: () -> Unit,
    onRenameTitleChange: (String) -> Unit,
    onRenameConfirm: () -> Unit,
    onRenameCancel: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onNewChatClick: () -> Unit,
    onModelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalWindowInfo.current.containerDpSize.width
    val drawerWidth = min(
        DrawerDefaults.MaximumDrawerWidth.value,
        (screenWidth - MinimumVisibleScrim).value,
    ).dp
    val groups = remember(sessions) {
        groupSessionsByRecency(sessions, System.currentTimeMillis(), ZoneId.systemDefault())
    }
    val listState = rememberLazyListState()

    ModalDrawerSheet(
        modifier = modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        DrawerHeader()

        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(R.string.drawer_new_chat),
                    style = MaterialTheme.typography.titleSmall
                )
            },
            icon = {
                Icon(
                    imageVector = MaterialTheme.icons.actions.newChat.filled,
                    contentDescription = null,
                )
            },
            selected = false,
            onClick = onNewChatClick,
            modifier = Modifier
                .padding(horizontal = Spacing.Medium)
                .height(48.dp),
            shape = MaterialTheme.shapes.large,
        )

        ConversationHistory(
            groups = groups,
            currentSessionId = currentSessionId,
            actionsFor = actionsFor,
            state = listState,
            onSessionClick = onSessionClick,
            onShowActions = onSessionLongPress,
            onActionsDismiss = onActionsDismiss,
            onRename = onRenameRequest,
            onDelete = onDeleteRequest,
            modifier = Modifier.weight(1f),
        )

        DrawerFooter(
            onModelsClick = onModelsClick,
            onSettingsClick = onSettingsClick,
        )
    }

    if (renaming != null) {
        RenameDialog(
            state = renaming,
            onTitleChange = onRenameTitleChange,
            onConfirm = onRenameConfirm,
            onDismiss = onRenameCancel,
        )
    }

    if (deleting != null) {
        DeleteDialog(
            session = deleting,
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteCancel,
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
                    text = stringResource(R.string.drawer_empty),
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
            item(key = group.recency, contentType = "recency") {
                SectionHeader(
                    title = stringResource(group.recency.labelRes),
                    modifier = Modifier
                        .animateItem()
                        .padding(top = if (groupIndex == 0) Spacing.Small else Spacing.Medium)
                )
            }
            itemsIndexed(
                items = group.sessions,
                key = { _, session -> session.id },
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
    val length = FadeLength.toPx()
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
                .height(44.dp)
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(
                    onClick = onClick, onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowActions()
                    }, onLongClickLabel = stringResource(R.string.drawer_conversation_actions)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (selected) MaterialTheme.icons.navigation.chat.filled
                    else MaterialTheme.icons.navigation.chat.outline,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 5.dp)
                )

                Text(
                    text = sessionDisplayTitle(session.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onShowActions,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = MaterialTheme.icons.actions.more.outline,
                        contentDescription = stringResource(R.string.drawer_conversation_actions),
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
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
            text = { Text(stringResource(R.string.drawer_rename)) },
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
                    text = stringResource(R.string.action_delete),
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
private fun DrawerFooter(
    onModelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
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
                    text = stringResource(R.string.models_title),
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
                contentDescription = stringResource(R.string.drawer_settings),
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
        title = { Text(stringResource(R.string.drawer_rename_title)) },
        text = {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text(stringResource(R.string.drawer_rename_field)) },
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
            ) { Text(stringResource(R.string.drawer_rename)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun DeleteDialog(session: ChatSession, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(MaterialTheme.icons.actions.delete.outline, contentDescription = null) },
        title = { Text(stringResource(R.string.drawer_delete_title)) },
        text = {
            Text(stringResource(R.string.drawer_delete_body, sessionDisplayTitle(session.title)))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AppDrawerPreview() {
    val now = System.currentTimeMillis()
    MishtiTheme {
        AppDrawer(
            sessions = listOf(
                ChatSession("1", "Running a model offline", 0L, now),
                ChatSession("2", "Haiku about the sea", 0L, now - 3_600_000L),
                ChatSession("3", "Dinner ideas", 0L, now - 86_400_000L),
                ChatSession("4", "Regex for email addresses", 0L, now - 4 * 86_400_000L),
                ChatSession("5", "Regex for email addresses", 0L, now - 4 * 86_400_000L),
            ),
            currentSessionId = "1",
            actionsFor = null,
            renaming = null,
            deleting = null,
            onSessionClick = {},
            onSessionLongPress = {},
            onActionsDismiss = {},
            onRenameRequest = {},
            onRenameTitleChange = {},
            onRenameConfirm = {},
            onRenameCancel = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onNewChatClick = {},
            onModelsClick = {},
            onSettingsClick = {},
        )
    }
}