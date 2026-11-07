package com.abrarshakhi.mishti.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.core.designsystem.resources.chat_error_new
import com.abrarshakhi.mishti.core.domain.chat.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.core.ui.drawer.DrawerController
import com.abrarshakhi.mishti.core.ui.mvi.CollectEffects
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes

@Composable
fun ChatRoute(sessionId: String? = null) {
    val drawerController: DrawerController = koinInject()
    val navigator: Navigator = koinInject()
    val createNewSession: CreateNewSessionUseCase = koinInject()
    val snackBarDispatcher: SnackBarDispatcher = koinInject()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: ChatViewModel = koinViewModel { parametersOf(sessionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    SideEffect { drawerController.onSessionVisible(state.sessionId) }

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is ChatEffect.ShowError -> snackBarDispatcher.showError(effect.text)
        }
    }

    ChatScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenDrawer = {
            coroutineScope.launch { drawerController.open() }
        },
        onNewChat = {
            coroutineScope.launch {
                try {
                    val newSessionId = createNewSession()
                    navigator.resetTo(Screen.Chat(newSessionId))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    snackBarDispatcher.showError(CoreDesignsystemRes.string.chat_error_new)
                }
            }
        },
        onOpenModels = { navigator.navigate(Screen.Models) },
    )
}
