package com.abrarshakhi.mishti.presentation.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatRoute(
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        state,
        effect = viewModel.effect,
        onIntent = viewModel::onIntent,
        onOpenModels = onOpenModels,
        onOpenSettings = onOpenSettings,
    )
}