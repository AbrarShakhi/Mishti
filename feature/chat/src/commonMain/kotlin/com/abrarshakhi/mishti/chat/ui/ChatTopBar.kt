package com.abrarshakhi.mishti.chat.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.core.designsystem.resources.drawer_new_chat
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.feature.chat.resources.chat_open_conversations
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes
import com.abrarshakhi.mishti.feature.chat.resources.Res as FeatureChatRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopBar(scrollBehavior: TopAppBarScrollBehavior, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    MaterialTheme.icons.navigation.menu.filled,
                    contentDescription = stringResource(FeatureChatRes.string.chat_open_conversations),
                )
            }
        },
        actions = {
            IconButton(onClick = onNewChat) {
                Icon(
                    MaterialTheme.icons.actions.newChat.filled,
                    contentDescription = stringResource(CoreDesignsystemRes.string.drawer_new_chat),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
