package com.abrarshakhi.mishti.core.ui.navigation

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data class Chat(val sessionId: String? = null) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object AppearanceSettings : Screen

    @Serializable
    data object ChatSettings : Screen

    @Serializable
    data object GenerationSettings : Screen

    @Serializable
    data object PerformanceSettings : Screen

    @Serializable
    data class Document(val document: AppDocument) : Screen

    @Serializable
    data object Models : Screen
}
