package com.abrarshakhi.mishti.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single past conversation shown in the drawer list.
 *
 * In a real app this would come from the database via ViewModel.
 * For now, it's a simple data holder so the UI is already shaped correctly.
 */
data class ConversationItem(
    val id: String,
    val title: String,
)

/**
 * Groups of conversations shown in the drawer, mirroring Claude's recency sections.
 */
data class ConversationGroup(
    val label: String,               // e.g. "Today", "Yesterday", "Last 7 days"
    val conversations: List<ConversationItem>,
)

/**
 * Claude-style navigation drawer for Mishti.
 *
 * Layout (top → bottom):
 *   ┌──────────────────────────────┐
 *   │  ＋  New chat                │  ← tappable row
 *   ├──────────────────────────────┤
 *   │  Today                       │
 *   │    Conversation title 1      │
 *   │    Conversation title 2      │
 *   │  Yesterday                   │
 *   │    …                         │
 *   │  Last 7 days                 │
 *   │    …                         │  ← scrollable middle zone
 *   ├──────────────────────────────┤
 *   │  ⚙  Settings                 │  ← always visible at bottom
 *   └──────────────────────────────┘
 *
 * @param groups            Recency-grouped conversation history.
 * @param activeId          ID of the currently open conversation (highlighted).
 * @param onNewChat         "New chat" row tapped.
 * @param onConversationClick  A past conversation row tapped.
 * @param onSettingsClick   Settings row tapped.
 */
@Composable
fun MishtiDrawer(
    groups: List<ConversationGroup>,
    activeId: String?,
    onNewChat: () -> Unit,
    onConversationClick: (ConversationItem) -> Unit,
    onSettingsClick: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
        ) {

            DrawerRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onNewChat,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New chat",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "New chat",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                groups.forEach { group ->
                    item(key = "header-${group.label}") {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 4.dp,
                            ),
                        )
                    }

                    items(group.conversations, key = { it.id }) { convo ->
                        ConversationRow(
                            item = convo,
                            isActive = convo.id == activeId,
                            onClick = { onConversationClick(convo) },
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            DrawerRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onSettingsClick,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Shared row layout used by "New chat", Settings, and individual conversations.
 * Gives each row a consistent height, padding, and the pill highlight on press.
 */
@Composable
private fun DrawerRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    content: @Composable () -> Unit,
) {
    val bgColor = if (isHighlighted)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainerLow

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        content()
    }
}

/**
 * A single conversation entry row.
 */
@Composable
private fun ConversationRow(
    item: ConversationItem,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    DrawerRow(
        modifier = Modifier.padding(horizontal = 8.dp),
        onClick = onClick,
        isHighlighted = isActive,
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}