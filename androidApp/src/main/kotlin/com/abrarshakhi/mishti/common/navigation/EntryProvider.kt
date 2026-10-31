package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.entryProvider
import com.abrarshakhi.mishti.features.chat.presentation.chatEntry
import com.abrarshakhi.mishti.features.models.presentation.modelsEntry
import com.abrarshakhi.mishti.features.onboarding.presentation.onboardingEntry
import com.abrarshakhi.mishti.features.settings.presentation.settingsEntry
import com.abrarshakhi.mishti.features.splash.presentation.splashEntry

fun navEntryProvider(onOpenDrawer: () -> Unit, onNewChat: () -> Unit) = entryProvider {
    splashEntry()
    onboardingEntry()
    chatEntry(onOpenDrawer, onNewChat)
    modelsEntry()
    settingsEntry()
}
