package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.entryProvider
import com.abrarshakhi.mishti.features.chat.presentation.chatEntry
import com.abrarshakhi.mishti.features.models.presentation.modelsEntry
import com.abrarshakhi.mishti.features.onboarding.presentation.onboardingEntry
import com.abrarshakhi.mishti.features.settings.presentation.settingsEntry

fun navEntryProvider(navigator: Navigator, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) = entryProvider {
    chatEntry(navigator, onOpenDrawer, onNewChat)
    modelsEntry(navigator)
    onboardingEntry(navigator)
    settingsEntry(navigator)
}
