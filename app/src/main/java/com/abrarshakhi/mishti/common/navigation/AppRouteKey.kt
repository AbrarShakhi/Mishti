package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {
    @Serializable
    data object Onboarding : AppRouteKey

    @Serializable
    data class Chat(val sessionId: String? = null) : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey

    @Serializable
    data object Models : AppRouteKey
}
