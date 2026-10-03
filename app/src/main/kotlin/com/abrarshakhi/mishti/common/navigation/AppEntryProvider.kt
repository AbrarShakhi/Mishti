package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.entryProvider
import com.abrarshakhi.mishti.features.chat.presentation.ChatRoute
import com.abrarshakhi.mishti.features.models.presentation.ModelsRoute
import com.abrarshakhi.mishti.features.onboarding.presentation.OnboardingRoute
import com.abrarshakhi.mishti.features.settings.presentation.SettingsRoute

fun appEntryProvider(
    navigator: Navigator,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
) = entryProvider {
    entry<AppRouteKey.Chat> { key ->
        ChatRoute(
            sessionId = key.sessionId,
            onOpenDrawer = onOpenDrawer,
            onNewChat = onNewChat,
        )
    }
    entry<AppRouteKey.Settings> {
        SettingsRoute(
            onBack = { navigator.goBack() },
            onNavigateToModels = { navigator.navigate(AppRouteKey.Models) },
        )
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
