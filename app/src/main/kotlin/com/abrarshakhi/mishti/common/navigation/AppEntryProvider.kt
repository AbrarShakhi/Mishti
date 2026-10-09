package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.entryProvider
import com.abrarshakhi.mishti.features.chat.presentation.ChatRoute
import com.abrarshakhi.mishti.features.models.presentation.ModelsRoute
import com.abrarshakhi.mishti.features.onboarding.presentation.OnboardingRoute
import com.abrarshakhi.mishti.features.settings.presentation.AppearanceSettingsRoute
import com.abrarshakhi.mishti.features.settings.presentation.ChatSettingsRoute
import com.abrarshakhi.mishti.features.settings.presentation.DocumentRoute
import com.abrarshakhi.mishti.features.settings.presentation.GenerationSettingsRoute
import com.abrarshakhi.mishti.features.settings.presentation.PerformanceSettingsRoute
import com.abrarshakhi.mishti.features.settings.presentation.SettingsRoute

fun appEntryProvider(navigator: Navigator, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) = entryProvider {
    entry<AppRouteKey.Chat> { key ->
        ChatRoute(
            sessionId = key.sessionId,
            navigator = navigator,
            onOpenDrawer = onOpenDrawer,
            onNewChat = onNewChat,
        )
    }
    entry<AppRouteKey.Settings> {
        SettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.Document> { key ->
        DocumentRoute(document = key.document, navigator = navigator)
    }
    entry<AppRouteKey.AppearanceSettings> {
        AppearanceSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.ChatSettings> {
        ChatSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.GenerationSettings> {
        GenerationSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.PerformanceSettings> {
        PerformanceSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.Models> {
        ModelsRoute(navigator = navigator)
    }
    entry<AppRouteKey.Onboarding> {
        OnboardingRoute(onFinished = { navigator.resetTo(AppRouteKey.Chat()) })
    }
}
