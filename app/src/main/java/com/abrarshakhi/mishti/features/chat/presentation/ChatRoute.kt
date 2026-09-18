package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import org.koin.compose.koinInject

@Composable
fun ChatRoute(
    sessionId: String?,
    modifier: Modifier = Modifier,
) {
    val snackbarDispatcher: SnackbarDispatcher = koinInject()

    ChatScreen(state = ChatUiState(), onIntent = {}, modifier = modifier)
}
