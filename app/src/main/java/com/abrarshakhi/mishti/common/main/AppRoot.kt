package com.abrarshakhi.mishti.common.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.mvi.CollectEffects
import com.abrarshakhi.mishti.common.navigation.AppNavigation
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.currentRoute
import com.abrarshakhi.mishti.common.navigation.navigateTo
import com.abrarshakhi.mishti.common.navigation.rememberAppBackStack
import com.abrarshakhi.mishti.common.navigation.switchTapTo
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.chat.presentation.SessionsEffect
import com.abrarshakhi.mishti.features.chat.presentation.SessionsIntent
import com.abrarshakhi.mishti.features.chat.presentation.SessionsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppRoot(startRoute: AppRouteKey, mainAppViewModel: MainAppViewModel) {
    val backStack = rememberAppBackStack(startRoute)
    val current = backStack.currentRoute()
    val currentChrome = current?.chrome()

    val scrollBehaviorTop = TopAppBarDefaults.pinnedScrollBehavior()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val sessionsViewModel: SessionsViewModel = koinViewModel()
    val sessionsState by sessionsViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarDispatcher: SnackbarDispatcher = koinInject()
    LaunchedEffect(snackbarDispatcher) {
        snackbarDispatcher.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message.text,
                withDismissAction = message.duration != SnackbarDuration.Short,
                duration = message.duration,
            )
        }
    }

    val visibleSessionId =
        (current as? AppRouteKey.Chat)?.sessionId ?: sessionsState.sessions.firstOrNull()?.id

    CollectEffects(sessionsViewModel.effects) { effect ->
        when (effect) {
            is SessionsEffect.OpenSession -> {
                drawerState.close()
                backStack.switchTapTo(AppRouteKey.Chat(effect.sessionId))
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    LaunchedEffect(current) {
        scrollBehaviorTop.state.contentOffset = 0f
        scrollBehaviorTop.state.heightOffset = 0f
    }

    val chromeScope = remember(backStack, scrollBehaviorTop, drawerState) {
        ChromeScope(
            backStack = backStack,
            scrollBehavior = scrollBehaviorTop,
            openDrawer = { coroutineScope.launch { drawerState.open() } },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = current is AppRouteKey.Chat || drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                sessions = sessionsState.sessions,
                currentSessionId = visibleSessionId,
                actionsFor = sessionsState.actionsFor,
                renaming = sessionsState.renaming,
                deleting = sessionsState.deleting,
                onSessionClick = { sessionsViewModel.onIntent(SessionsIntent.SessionSelected(it)) },
                onSessionLongPress = {
                    sessionsViewModel.onIntent(SessionsIntent.SessionLongPressed(it))
                },
                onActionsDismiss = { sessionsViewModel.onIntent(SessionsIntent.ActionsDismissed) },
                onRenameRequest = { sessionsViewModel.onIntent(SessionsIntent.RenameRequested) },
                onRenameTitleChange = {
                    sessionsViewModel.onIntent(SessionsIntent.RenameTitleChanged(it))
                },
                onRenameConfirm = { sessionsViewModel.onIntent(SessionsIntent.RenameConfirmed) },
                onRenameCancel = { sessionsViewModel.onIntent(SessionsIntent.RenameCancelled) },
                onDeleteRequest = { sessionsViewModel.onIntent(SessionsIntent.DeleteRequested) },
                onDeleteConfirm = {
                    sessionsViewModel.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId))
                },
                onDeleteCancel = { sessionsViewModel.onIntent(SessionsIntent.DeleteCancelled) },
                onNewChatClick = {
                    sessionsViewModel.onIntent(SessionsIntent.NewChatClicked)
                },
                onModelsClick = {
                    coroutineScope.launch { drawerState.close() }
                    backStack.navigateTo(AppRouteKey.Models)
                },
                onSettingsClick = {
                    coroutineScope.launch { drawerState.close() }
                    backStack.navigateTo(AppRouteKey.Settings)
                },
            )
        },
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehaviorTop.nestedScrollConnection),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { currentChrome?.topBar?.invoke(chromeScope) },
            floatingActionButton = { currentChrome?.fab?.invoke(chromeScope) },
        ) { innerPadding ->
            AppNavigation(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                mainAppViewModel = mainAppViewModel,
            )
        }
    }
}
