package com.abrarshakhi.mishti.common.navigation

import com.abrarshakhi.mishti.features.settings.presentation.AppDocument
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey {
    @Serializable
    data object Onboarding : AppRouteKey

    @Serializable
    data class Chat(val sessionId: String? = null) : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey

    @Serializable
    data object AppearanceSettings : AppRouteKey

    @Serializable
    data object ChatSettings : AppRouteKey

    @Serializable
    data object GenerationSettings : AppRouteKey

    @Serializable
    data object PerformanceSettings : AppRouteKey

    @Serializable
    data class Document(val document: AppDocument) : AppRouteKey

    @Serializable
    data object Models : AppRouteKey
}
