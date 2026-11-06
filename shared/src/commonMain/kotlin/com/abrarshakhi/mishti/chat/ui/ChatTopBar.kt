package com.abrarshakhi.mishti.chat.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.chat_open_conversations
import com.abrarshakhi.mishti.shared.resources.drawer_new_chat
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopBar(scrollBehavior: TopAppBarScrollBehavior, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    MaterialTheme.icons.navigation.menu.filled,
                    contentDescription = stringResource(Res.string.chat_open_conversations),
                )
            }
        },
        actions = {
            IconButton(onClick = onNewChat) {
                Icon(
                    MaterialTheme.icons.actions.newChat.filled,
                    contentDescription = stringResource(Res.string.drawer_new_chat),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
