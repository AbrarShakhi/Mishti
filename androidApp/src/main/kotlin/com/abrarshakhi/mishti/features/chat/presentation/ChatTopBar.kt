package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.icons

@Composable
internal fun ChatTopBar(scrollBehavior: TopAppBarScrollBehavior, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    MaterialTheme.icons.navigation.menu.filled,
                    contentDescription = stringResource(R.string.chat_open_conversations),
                )
            }
        },
        actions = {
            IconButton(onClick = onNewChat) {
                Icon(
                    MaterialTheme.icons.actions.newChat.filled,
                    contentDescription = stringResource(R.string.drawer_new_chat),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
