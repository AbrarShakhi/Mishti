package com.abrarshakhi.mishti.core.ui.drawer

import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.abrarshakhi.mishti.core.session.ui.SessionsEffect
import com.abrarshakhi.mishti.core.session.ui.SessionsIntent
import com.abrarshakhi.mishti.core.session.ui.SessionsViewModel
import com.abrarshakhi.mishti.core.session.ui.groupSessionsByRecency
import com.abrarshakhi.mishti.core.ui.mvi.CollectEffects
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.core.utils.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MishtiNavigationDrawer(content: @Composable () -> Unit) {
    val navigator: Navigator = koinInject()
    val drawerController: DrawerController = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel: SessionsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentScreen = navigator.currentScreen
    val visibleSessionId = if (currentScreen is Screen.Chat) {
        currentScreen.sessionId ?: drawerController.activeSessionId
    } else {
        null
    }
    val isOpen = drawerController.isOpen
    val now by produceState(currentTimeMillis(), isOpen) {
        value = currentTimeMillis()
        if (isOpen) {
            while (true) {
                delay(60_000.milliseconds)
                value = currentTimeMillis()
            }
        }
    }
    val groups = remember(state.sessions, now) { groupSessionsByRecency(state.sessions, now) }

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is SessionsEffect.OpenSession -> {
                navigator.resetTo(Screen.Chat(effect.sessionId))
                scope.launch { drawerController.close() }
            }
        }
    }

    LaunchedEffect(isOpen) {
        if (!isOpen) viewModel.onIntent(SessionsIntent.DrawerClosed)
    }
    val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)

    fun navigate(screen: Screen) {
        navigator.navigate(screen)
        scope.launch { drawerController.close() }
    }

    DismissibleNavigationDrawer(
        drawerState = drawerController.state,
        gesturesEnabled = currentScreen is Screen.Chat || isOpen,
        drawerContent = {
            AppDrawer(
                state = state,
                groups = groups,
                currentSessionId = visibleSessionId,
                onIntent = viewModel::onIntent,
                onModelsClick = { navigate(Screen.Models) },
                onSettingsClick = { navigate(Screen.Settings) },
            )
        },
        content = {
            content()
            NavigationBackHandler(
                state = navigationEventState,
                isBackEnabled = isOpen,
                onBackCompleted = {
                    when {
                        state.deleting != null -> viewModel.onIntent(SessionsIntent.DeleteCancelled)
                        state.renaming != null -> viewModel.onIntent(SessionsIntent.RenameCancelled)
                        state.actionsFor != null -> viewModel.onIntent(SessionsIntent.ActionsDismissed)
                        else -> scope.launch { drawerController.close() }
                    }
                },
            )
        },
    )
}
