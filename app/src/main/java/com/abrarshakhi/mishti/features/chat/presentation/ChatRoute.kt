package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.mvi.CollectEffects
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf


@Composable
fun ChatRoute(
    sessionId: String?,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatViewModel = koinViewModel { parametersOf(sessionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarDispatcher: SnackbarDispatcher = koinInject()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is ChatEffect.ShowError -> snackbarDispatcher.showError(effect.text)
        }
    }

    ChatScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
