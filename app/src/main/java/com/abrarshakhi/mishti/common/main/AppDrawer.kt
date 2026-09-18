package com.abrarshakhi.mishti.common.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.presentation.RenameState
import kotlin.math.min

private val MinimumVisibleScrim = 56.dp

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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = min(
        DrawerDefaults.MaximumDrawerWidth.value,
        (screenWidth - MinimumVisibleScrim).value,
    ).dp

    ModalDrawerSheet(modifier = modifier.width(drawerWidth)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            )

            NavigationDrawerItem(
                label = { Text("New chat") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                selected = false,
                onClick = onNewChatClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                items(items = sessions, key = { it.id }) { session ->
                    ConversationItem(
                        session = session,
                        selected = session.id == currentSessionId,
                        menuExpanded = actionsFor?.id == session.id,
                        onClick = { onSessionClick(session.id) },
                        onLongClick = { onSessionLongPress(session.id) },
                        onMenuDismiss = onActionsDismiss,
                        onRename = onRenameRequest,
                        onDelete = onDeleteRequest,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))

            NavigationDrawerItem(
                label = { Text("Models") },
                icon = {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                },
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
                    .padding(bottom = 8.dp),
            )
        }
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
private fun ConversationItem(
    session: ChatSession,
    selected: Boolean,
    menuExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    Box(modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
        Surface(
            color = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
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
                            onLongClick()
                        },
                    )
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename conversation") },
        text = {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text("Title") },
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
        title = { Text("Delete conversation?") },
        text = { Text("\"${session.title}\" and all of its messages will be deleted. This cannot be undone.") },
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
    MishtiTheme {
        AppDrawer(
            sessions = listOf(
                ChatSession("1", "Running a model offline", 0L, 2L),
                ChatSession("2", "New chat", 0L, 1L),
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
