package com.abrarshakhi.mishti.common.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.mishti.common.main.MainAppViewModel
import com.abrarshakhi.mishti.features.chat.presentation.ChatRoute
import com.abrarshakhi.mishti.features.models.presentation.ModelsRoute
import com.abrarshakhi.mishti.features.onboarding.presentation.OnboardingRoute
import com.abrarshakhi.mishti.features.settings.presentation.SettingsRoute


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    backStack: SnapshotStateList<AppRouteKey>,
    modifier: Modifier = Modifier,
    mainAppViewModel: MainAppViewModel,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AppRouteKey.Chat> { key ->
                ChatRoute(sessionId = key.sessionId)
            }
            entry<AppRouteKey.Settings> {
                SettingsRoute(onNavigateToModels = { backStack.navigateTo(AppRouteKey.Models) })
            }

            entry<AppRouteKey.Models> {
                ModelsRoute()
            }
            entry<AppRouteKey.Onboarding> {
                OnboardingRoute(
                    onFinished = { backStack.switchTapTo(AppRouteKey.Chat()) },
                )
            }
        },
    )
}
