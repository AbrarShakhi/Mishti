package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {
    @Serializable
    data object Onboarding : NavKey

    @Serializable
    data class Chat(val sessionId: String? = null) : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object Models : NavKey
}
