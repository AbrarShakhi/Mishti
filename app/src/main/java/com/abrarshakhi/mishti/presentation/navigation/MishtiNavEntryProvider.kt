package com.abrarshakhi.mishti.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.mishti.presentation.screens.ChatHistoryScreen
import com.abrarshakhi.mishti.presentation.screens.ChatScreen
import com.abrarshakhi.mishti.presentation.screens.ModelPickerScreen
import com.abrarshakhi.mishti.presentation.screens.SettingsScreen

/**
 * Maps every [NavRoutes] key to a [NavEntry] that contains its composable content.
 *
 * This is passed to [NavDisplay] as the `entryProvider` lambda.
 * Adding a new screen = add a route in [NavRoutes] + a branch here.
 *
 * @param key: [NavKey] current navKey.
 * @param backStack: [NavBackStack] reference to the live back-stack so screens can navigate.
 */
fun mishtiEntryProvider(
    key: NavKey,
    backStack: NavBackStack<NavKey>
): NavEntry<NavKey> =
    when (key) {
        is NavRoutes.Chat -> NavEntry(key) {
            ChatScreen(
                onOpenHistory = { backStack.add(NavRoutes.ChatHistory) },
                onOpenModels = { backStack.add(NavRoutes.ModelPicker) },
                onNewChat = { /* TODO: clear current chat state */ },
            )
        }

        is NavRoutes.ChatHistory -> NavEntry(key) {
            ChatHistoryScreen(
                onClose = { backStack.removeLastOrNull() },
                onOpenSettings = {
                    backStack.removeLastOrNull()
                    backStack.add(NavRoutes.Settings)
                },
                onSelectChat = {
                    backStack.removeLastOrNull()
                    // TODO: tell ChatViewModel to load chatId
                },
            )
        }

        is NavRoutes.ModelPicker -> NavEntry(key) {
            ModelPickerScreen(
                onDismiss = { backStack.removeLastOrNull() },
                onModelSelected = { /*modelId ->*/
                    backStack.removeLastOrNull()
                    // TODO: tell ChatViewModel to switch model
                },
            )
        }

        is NavRoutes.Settings -> NavEntry(key) {
            SettingsScreen(
                onBack = { backStack.removeLastOrNull() },
            )
        }

        else -> throw RuntimeException("Invalid NavKey: $key")
    }
