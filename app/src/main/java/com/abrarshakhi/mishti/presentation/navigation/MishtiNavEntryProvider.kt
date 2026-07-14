package com.abrarshakhi.mishti.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.mishti.presentation.screens.ChatScreen
import com.abrarshakhi.mishti.presentation.screens.ModelPickerScreen
import com.abrarshakhi.mishti.presentation.screens.SettingsScreen

/**
 * Maps every [NavRoutes] key to a [NavEntry] that contains its composable content.
 *
 * This is passed to [NavDisplay] as the `entryProvider` lambda.
 * Adding a new screen = add a route in [NavRoutes] + a branch here.
 *
 * @param route: [NavRoutes] current navKey.
 * @param backStack: [NavBackStack] reference to the live back-stack so screens can navigate.
 */
fun mishtiEntryProvider(
    route: NavRoutes,
    backStack: NavBackStack<NavKey>
): NavEntry<NavKey> =
    when (route) {
        is NavRoutes.Chat -> NavEntry(route) {
            ChatScreen(
                onOpenHistory = { },
                onOpenModels = { },
                onNewChat = { },
            )
        }

        is NavRoutes.ModelPicker -> NavEntry(route) {
            ModelPickerScreen(
                onDismiss = { },
                onModelSelected = { },
            )
        }

        is NavRoutes.Settings -> NavEntry(route) {
            SettingsScreen(
                onBack = { },
            )
        }
    }
