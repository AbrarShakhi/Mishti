package com.abrarshakhi.mishti.presentation.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Sealed class defining every destination in Mishti.
 *
 * Nav3 works with plain Kotlin objects as back-stack keys — no annotation
 * processors, no string routes. Each data object / data class here IS the key.
 */
sealed class NavRoutes : NavKey {

    /** The main chat screen. This is always the root of the back stack. */
    data object Chat : NavRoutes()

    /**
     * Slide-in panel showing the list of past conversations plus a
     * Settings entry at the bottom.
     * Rendered as a ModalDrawer over the Chat screen — still on the back
     * stack so the hardware back button / predictive-back gesture closes it.
     */
    data object ChatHistory : NavRoutes()

    /**
     * Model picker bottom sheet / full-screen dialog.
     * Opened from the top-bar center button while on Chat.
     */
    data object ModelPicker : NavRoutes()

    /**
     * Settings screen, reached from the bottom of the ChatHistory panel.
     */
    data object Settings : NavRoutes()
}