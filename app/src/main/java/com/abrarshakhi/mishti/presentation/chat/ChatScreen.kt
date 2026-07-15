package com.abrarshakhi.mishti.presentation.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abrarshakhi.mishti.presentation.components.ConversationGroup
import com.abrarshakhi.mishti.presentation.components.ConversationItem
import com.abrarshakhi.mishti.presentation.components.MishtiDrawer
import kotlinx.coroutines.launch

/**
 * Main chat screen — root destination.
 *
 * Top bar layout (left → right):
 *   [☰ Menu]  ←───────── [Model name] ─────────→  [＋ New chat]
 *
 * The navigation drawer is [MishtiDrawer]; this screen owns only
 * the drawer open/close state, everything else is delegated upward
 * via the callback parameters.
 *
 * @param conversationGroups  History passed down from ViewModel (empty list = no history yet).
 * @param activeConversationId  ID of the currently visible conversation; used to highlight the
 *                              correct row in the drawer.
 * @param onNewChat           Plus icon or drawer "New chat" tapped → start a fresh conversation.
 * @param onConversationClick A past conversation row tapped → load that conversation.
 * @param onOpenModels        Model name in the top bar tapped → open ModelPicker.
 * @param onOpenSettings      Settings entry in the drawer tapped → open Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationGroups: List<ConversationGroup> = emptyList(),
    activeConversationId: String? = null,
    onNewChat: () -> Unit,
    onConversationClick: (ConversationItem) -> Unit = {},
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MishtiDrawer(
                groups = conversationGroups,
                activeId = activeConversationId,
                onNewChat = {
                    scope.launch { drawerState.close() }
                    onNewChat()
                },
                onConversationClick = { convo ->
                    scope.launch { drawerState.close() }
                    onConversationClick(convo)
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onOpenSettings()
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Open menu")
                        }
                    },
                    title = {
                        // Tapping the model name opens the model picker.
                        // Replace "TinyLlama" with the active model name from ViewModel.
                        Text(
                            text = "TinyLlama",
                            modifier = Modifier.noRippleClickable { onOpenModels() },
                        )
                    },
                    actions = {
                        IconButton(onClick = onNewChat) {
                            Icon(Icons.Outlined.Add, contentDescription = "New chat")
                        }
                    },
                )
            },
        ) { innerPadding ->
            // ── Chat content goes here ────────────────────────────────────────
            // Replace with your message list + input bar once ready.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Chat goes here")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Makes any [Modifier] clickable without the ripple ink splash.
 * Used for the model-name tap target in the top bar, where a ripple
 * would look odd on text.
 */
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = null,
            onClick = onClick,
        )
    )