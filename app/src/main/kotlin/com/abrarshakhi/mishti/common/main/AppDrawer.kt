package com.abrarshakhi.mishti.common.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.abrarshakhi.mishti.common.ui.components.SectionHeader
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
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
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        DrawerHeader()

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_new_chat)) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = onNewChatClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
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
            start = Spacing.ExtraLarge,
            end = Spacing.ExtraLarge,
            top = Spacing.ExtraLarge,
            bottom = Spacing.Large,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppMark(size = 40.dp)
        Spacer(Modifier.width(Spacing.Medium))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        item(key = TopAnchorKey) {
            Spacer(Modifier.height(Spacing.Small - ListItemDefaults.SegmentedGap))
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
                        .padding(top = if (groupIndex == 0) 0.dp else Spacing.Medium),
                )
            }
            itemsIndexed(
                items = group.sessions,
                key = { _, session -> session.id },
                contentType = { _, _ -> "session" },
            ) { index, session ->
                ConversationItem(
                    session = session,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = group.sessions.size,
                    ),
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
    compositingStrategy =
        CompositingStrategy.Offscreen
}
    .drawWithContent {
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
    shapes: ListItemShapes,
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

    SegmentedListItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        shapes = shapes,
        onLongClick = {
            haptics.performHapticFeedback(
                HapticFeedbackType.LongPress,
            )
            onShowActions()
        },
        onLongClickLabel = stringResource(
            R.string.drawer_conversation_actions,
        ),
        trailingContent = if (selected) {
            {
                Box {
                    IconButton(
                        onClick = onShowActions,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(
                                R.string.drawer_conversation_actions,
                            ),
                        )
                    }

                    ConversationActionsMenu(
                        expanded = menuExpanded,
                        onDismiss = onMenuDismiss,
                        onRename = onRename,
                        onDelete = onDelete,
                    )
                }
            }
        } else {
            null
        },
    ) {
        Text(
            text = sessionDisplayTitle(session.title),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.drawer_rename))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                )
            },
            onClick = {
                onDismiss()
                onRename()
            },
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
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = colorScheme.error,
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            },
        )
    }
}

@Composable
private fun DrawerFooter(onModelsClick: () -> Unit, onSettingsClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(R.string.models_title)) },
        icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
        selected = false,
        onClick = onModelsClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
    NavigationDrawerItem(
        label = { Text(stringResource(R.string.drawer_settings)) },
        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
        selected = false,
        onClick = onSettingsClick,
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .padding(bottom = Spacing.Medium),
    )
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
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
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
        icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
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
//                ChatSession("2", "Haiku about the sea", 0L, now - 3_600_000L),
//                ChatSession("3", "Dinner ideas", 0L, now - 86_400_000L),
//                ChatSession("4", "Regex for email addresses", 0L, now - 4 * 86_400_000L),
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
