package com.abrarshakhi.mishti.common.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.mishti.common.mvi.CollectEffects
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.Navigator
import com.abrarshakhi.mishti.common.navigation.TOP_LEVEL_ROUTES
import com.abrarshakhi.mishti.common.navigation.appEntryProvider
import com.abrarshakhi.mishti.common.navigation.rememberNavigationState
import com.abrarshakhi.mishti.common.navigation.rememberSharedAxisTransition
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.chat.presentation.SessionsEffect
import com.abrarshakhi.mishti.features.chat.presentation.SessionsIntent
import com.abrarshakhi.mishti.features.chat.presentation.SessionsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppShell(startRoute: NavKey) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navigationState = rememberNavigationState(
        startRoute = startRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES,
    )
    val navigator = remember { Navigator(navigationState) }

    val sessionsViewModel: SessionsViewModel = koinViewModel()
    val sessionsState by sessionsViewModel.state.collectAsStateWithLifecycle()

    val entryProvider = remember(navigator) {
        appEntryProvider(
            navigator = navigator,
            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
            onNewChat = { sessionsViewModel.onIntent(SessionsIntent.NewChatClicked) },
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val snackbarDispatcher: SnackbarDispatcher = koinInject()
    LaunchedEffect(snackbarDispatcher) {
        snackbarDispatcher.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message.text.resolve(resources),
                withDismissAction = message.duration != SnackbarDuration.Short,
                duration = message.duration,
            )
        }
    }

    val visibleSessionId =
        (navigationState.currentRoute as? AppRouteKey.Chat)?.sessionId
            ?: sessionsState.sessions.firstOrNull()?.id

    CollectEffects(sessionsViewModel.effects) { effect ->
        when (effect) {
            is SessionsEffect.OpenSession -> {
                drawerState.close()
                navigator.resetTo(AppRouteKey.Chat(effect.sessionId))
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = navigationState.currentRoute is AppRouteKey.Chat || drawerState.isOpen,
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
                    navigator.navigate(AppRouteKey.Models)
                },
                onSettingsClick = {
                    coroutineScope.launch { drawerState.close() }
                    navigator.navigate(AppRouteKey.Settings)
                },
            )
        },
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            ),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            val forward = rememberSharedAxisTransition(forward = true)
            val backward = rememberSharedAxisTransition(forward = false)

            NavDisplay(
                entries = navigationState.toDecoratedEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                transitionSpec = { forward },
                popTransitionSpec = { backward },
                predictivePopTransitionSpec = { backward },
            )
        }
    }
}
