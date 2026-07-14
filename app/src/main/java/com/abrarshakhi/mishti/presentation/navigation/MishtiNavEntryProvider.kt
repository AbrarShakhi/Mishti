package com.abrarshakhi.mishti.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.mishti.presentation.screens.ChatScreen
import com.abrarshakhi.mishti.presentation.screens.ModelPickerScreen
import com.abrarshakhi.mishti.presentation.screens.SettingsScreen

/**
 * Maps every [NavRoutes] key to a [NavEntry] that contains its composable content.
 *
 * This is the single place where screens receive their navigation callbacks.
 * Adding a new screen means:
 *   1. Add a route object in [NavRoutes].
 *   2. Add a branch here.
 *   3. Implement the screen composable.
 *
 * @param route     The current [NavRoutes] key being resolved.
 * @param backStack The live back-stack; mutate it to navigate.
 */
fun mishtiEntryProvider(
    route: NavRoutes,
    backStack: NavBackStack<NavKey>
): NavEntry<NavKey> =
    when (route) {

        is NavRoutes.Chat -> NavEntry(route) {
            ChatScreen(
                // Provide real data from a ViewModel in a future step.
                // For now the drawer renders in empty-state mode.
                conversationGroups = emptyList(),
                activeConversationId = null,
                onNewChat = {
                    // Pop everything above Chat so we're back at a blank slate,
                    // then let the ViewModel create a new conversation.
                    // For now, it's a no-op; replace with ViewModel call.
                },
                onConversationClick = { _ ->
                    // Load the tapped conversation via ViewModel.
                },
                onOpenModels = {
                    backStack.add(NavRoutes.ModelPicker)
                },
                onOpenSettings = {
                    backStack.add(NavRoutes.Settings)
                },
            )
        }

        is NavRoutes.ModelPicker -> NavEntry(route) {
            ModelPickerScreen(
                onDismiss = {
                    backStack.removeLastOrNull()
                },
                onModelSelected = { _ ->
                    // Pass selected model to ChatViewModel, then dismiss.
                    backStack.removeLastOrNull()
                },
            )
        }

        is NavRoutes.Settings -> NavEntry(route) {
            SettingsScreen(
                onBack = {
                    backStack.removeLastOrNull()
                },
            )
        }
    }