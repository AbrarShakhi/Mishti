package com.abrarshakhi.mishti.core.ui.drawer

import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.abrarshakhi.mishti.core.ui.mvi.CollectEffects
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MishtiNavigationDrawer(currentScreen: Screen?, content: @Composable () -> Unit) {
    val drawerController = koinInject<DrawerController>()
    val coroutineScope = rememberCoroutineScope()

//    val sessionsViewModel = koinViewModel<SessionsViewModel>()
//    val sessionsState by sessionsViewModel.state.collectAsStateWithLifecycle()

//    val visibleSessionId =
//        (currentScreen as? Screen.Chat)?.sessionId ?: sessionsState.sessions.firstOrNull()?.id

//    CollectEffects(sessionsViewModel.effects) { effect ->
//        when (effect) {
//            is SessionsEffect.OpenSession -> {
//                drawerState.close()
//                navigator.resetTo(AppRouteKey.Chat(effect.sessionId))
//            }
//        }
//    }

    val navigationEventState = rememberNavigationEventState(
        currentInfo = NavigationEventInfo.None,
    )

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = drawerController.isOpen,
        onBackCompleted = {
            coroutineScope.launch { drawerController.close() }
        },
    )

    DismissibleNavigationDrawer(
        drawerContent = { DrawerContent() },
        drawerState = drawerController.state,
        gesturesEnabled = currentScreen is Screen.Chat || drawerController.isOpen,
        content = content,
    )
}

@Composable
private fun DrawerContent() {
}
