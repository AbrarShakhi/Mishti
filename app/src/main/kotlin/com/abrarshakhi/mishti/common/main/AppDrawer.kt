package com.abrarshakhi.mishti.common.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.presentation.RenameState
import com.abrarshakhi.mishti.features.chat.presentation.groupSessionsByRecency
import java.time.ZoneId
import kotlin.math.min

private val MinimumVisibleScrim = 56.dp

/** Where drawer text and icons start: the item inset plus the item's own start padding. */
private val DrawerContentInset = 28.dp

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

    ModalDrawerSheet(modifier = modifier.width(drawerWidth)) {
        DrawerHeader()

        NavigationDrawerItem(
            label = { Text("New chat") },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = onNewChatClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = Spacing.Small),
        ) {
            groups.forEach { group ->
                item(key = group.recency) {
                    Text(
                        text = group.recency.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(
                                start = DrawerContentInset,
                                end = DrawerContentInset,
                                top = Spacing.Large,
                                bottom = Spacing.Small,
                            )
                            .semantics { heading() },
                    )
                }
                items(items = group.sessions, key = { it.id }) { session ->
                    ConversationItem(
                        session = session,
                        selected = session.id == currentSessionId,
                        menuExpanded = actionsFor?.id == session.id,
                        onClick = { onSessionClick(session.id) },
                        onShowActions = { onSessionLongPress(session.id) },
                        onMenuDismiss = onActionsDismiss,
                        onRename = onRenameRequest,
                        onDelete = onDeleteRequest,
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = DrawerContentInset))
        Spacer(Modifier.height(Spacing.Small))

        NavigationDrawerItem(
            label = { Text("Models") },
            icon = { Icon(Icons.Filled.Memory, contentDescription = null) },
            selected = false,
            onClick = onModelsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            selected = false,
            onClick = onSettingsClick,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(bottom = Spacing.Medium),
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * One conversation, drawn like a navigation drawer item. A long press opens its actions, and
 * the open conversation also shows a menu button so the actions can be found without one.
 */
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
) {
    val haptics = LocalHapticFeedback.current

    Box(modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
        Surface(
            color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShowActions()
                        },
                        onLongClickLabel = "Conversation actions",
                    )
                    .padding(start = Spacing.Large, end = Spacing.ExtraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (selected) {
                    IconButton(onClick = onShowActions) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Conversation actions")
                    }
                }
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = onMenuDismiss) {
            DropdownMenuItem(
                text = { Text("Rename") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = onRename,
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                ),
                onClick = onDelete,
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
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = { Text("Rename conversation") },
        text = {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (state.canConfirm) onConfirm() }),
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.canConfirm) { Text("Rename") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeleteDialog(
    session: ChatSession,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
        title = { Text("Delete conversation?") },
        text = {
            Text("\"${session.title}\" and all of its messages will be deleted. This can't be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
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
                ChatSession("2", "Haiku about the sea", 0L, now - 86_400_000L),
                ChatSession("3", "Dinner ideas", 0L, now - 4 * 86_400_000L),
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
