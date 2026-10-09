package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.mishti.features.settings.presentation.AppDocument
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
    data object AppearanceSettings : NavKey

    @Serializable
    data object ChatSettings : NavKey

    @Serializable
    data object GenerationSettings : NavKey

    @Serializable
    data object PerformanceSettings : NavKey

    @Serializable
    data class Document(val document: AppDocument) : NavKey

    @Serializable
    data object Models : NavKey
}
