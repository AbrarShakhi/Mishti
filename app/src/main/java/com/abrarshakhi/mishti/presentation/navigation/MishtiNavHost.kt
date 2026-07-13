package com.abrarshakhi.mishti.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Root navigation composable for Mishti.
 *
 * Owns the back stack and hands off to [NavDisplay] from Navigation 3.
 *
 * Back-stack initial state: [NavRoutes.Chat].
 * The Chat screen is always the bottom entry; all other routes are layered
 * on top so the hardware / gesture back closes them and returns to chat.
 *
 * Navigation graph:
 *
 *   Chat  ──(hamburger)──▶  ChatHistory  ──(settings)──▶  Settings
 *     │
 *     └──(model button)──▶  ModelPicker
 *
 * Overlay destinations (ChatHistory, ModelPicker) live on the back stack
 * so predictive-back works automatically with no extra wiring.
 */
@Composable
fun MishtiNavHost() {
    val backStack = rememberNavBackStack(NavRoutes.Chat)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = {
            mishtiEntryProvider(key = it, backStack = backStack)
        }
    )
}