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
            onOpenDrawer = onOpenDrawer,
            onNewChat = onNewChat,
            onOpenModels = { navigator.navigate(AppRouteKey.Models) },
        )
    }
    entry<AppRouteKey.Settings> {
        SettingsRoute(
            onBack = { navigator.goBack() },
            onOpenModels = { navigator.navigate(AppRouteKey.Models) },
            onOpenAppearance = { navigator.navigate(AppRouteKey.AppearanceSettings) },
            onOpenChat = { navigator.navigate(AppRouteKey.ChatSettings) },
            onOpenGeneration = { navigator.navigate(AppRouteKey.GenerationSettings) },
            onOpenPerformance = { navigator.navigate(AppRouteKey.PerformanceSettings) },
            onOpenDocument = { navigator.navigate(AppRouteKey.Document(it)) },
        )
    }
    entry<AppRouteKey.Document> { key ->
        DocumentRoute(
            document = key.document,
            onBack = { navigator.goBack() },
            onOpenDocument = { navigator.navigate(AppRouteKey.Document(it)) },
        )
    }
    entry<AppRouteKey.AppearanceSettings> {
        AppearanceSettingsRoute(onBack = { navigator.goBack() })
    }
    entry<AppRouteKey.ChatSettings> {
        ChatSettingsRoute(onBack = { navigator.goBack() })
    }
    entry<AppRouteKey.GenerationSettings> {
        GenerationSettingsRoute(onBack = { navigator.goBack() })
    }
    entry<AppRouteKey.PerformanceSettings> {
        PerformanceSettingsRoute(onBack = { navigator.goBack() })
    }
    entry<AppRouteKey.Models> {
        ModelsRoute(onBack = { navigator.goBack() })
    }
    entry<AppRouteKey.Onboarding> {
        OnboardingRoute(
            onFinished = { navigator.resetTo(AppRouteKey.Chat()) },
        )
    }
}
