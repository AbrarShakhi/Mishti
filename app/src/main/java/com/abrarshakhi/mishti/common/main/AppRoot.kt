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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.abrarshakhi.mishti.common.navigation.AppNavigation
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.currentRoute
import com.abrarshakhi.mishti.common.navigation.rememberAppBackStack
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AppRoot(startRoute: AppRouteKey, mainAppViewModel: MainAppViewModel) {
    val backStack = rememberAppBackStack(startRoute)
    val current = backStack.currentRoute()
    val currentChrome = current?.chrome()

    val scrollBehaviorTop = TopAppBarDefaults.pinnedScrollBehavior()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
        drawerContent = {},
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
