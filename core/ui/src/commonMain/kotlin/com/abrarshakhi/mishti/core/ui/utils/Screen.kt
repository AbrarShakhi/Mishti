package com.abrarshakhi.mishti.core.ui.utils

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen")
sealed interface Screen {
    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Splash")
    data object Splash : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Onboarding")
    data object Onboarding : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Chat")
    data class Chat(val sessionId: String? = null) :
        Screen,
        DrawerGestural

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Settings")
    data object Settings : Screen, DrawerGestural

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.AppearanceSettings")
    data object AppearanceSettings : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.ChatSettings")
    data object ChatSettings : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.GenerationSettings")
    data object GenerationSettings : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.PerformanceSettings")
    data object PerformanceSettings : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Document")
    data class Document(val document: AppDocument) : Screen

    @Serializable
    @SerialName("com.abrarshakhi.mishti.core.ui.navigation.Screen.Models")
    data object Models : Screen, DrawerGestural
}
