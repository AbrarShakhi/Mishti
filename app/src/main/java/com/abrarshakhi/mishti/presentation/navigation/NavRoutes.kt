package com.abrarshakhi.mishti.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed interface defining every destination in Mishti.
 */
@Serializable
sealed class NavRoutes : NavKey {

    /** The main chat screen. This is always the root of the back stack.
     * It has navigation drawer showing the list of past conversations plus a
     * Settings entry at the bottom.
     * stack so the hardware back button / predictive-back gesture closes it.
     */
    @Serializable
    data object Chat : NavRoutes()

    /**
     * Model picker/selection bottom sheet / full-screen dialog.
     * Opened from the top-bar center button while on Chat.
     */
    @Serializable
    data object ModelPicker : NavRoutes()

    /**
     * Settings screen, reached from the bottom of the ChatHistory panel.
     */
    @Serializable
    data object Settings : NavRoutes()
}