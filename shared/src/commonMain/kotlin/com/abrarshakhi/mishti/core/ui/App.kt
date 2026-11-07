package com.abrarshakhi.mishti.core.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.ui.drawer.MishtiNavigationDrawer
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.navigation.rememberSharedAxisTransition
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarProvider
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    MishtiTheme(
        koinViewModel<AppViewModel>()
            .themeSettings.collectAsStateWithLifecycle().value,
    ) {
        SnackBarProvider {
            val navigator: Navigator = koinInject()
            MishtiNavigationDrawer {
                val forward = rememberSharedAxisTransition(forward = true)
                val backward = rememberSharedAxisTransition(forward = false)
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    transitionSpec = { forward },
                    popTransitionSpec = { backward },
                    predictivePopTransitionSpec = { backward },
                    entryProvider = koinEntryProvider(),
                )
            }
        }
    }
}
