package com.abrarshakhi.mishti.common.main

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.features.chat.presentation.chatChrome
import com.abrarshakhi.mishti.features.onboarding.presentation.onboardingChrome
import com.abrarshakhi.mishti.features.settings.presentation.settingsChrome

data class ScreenChrome(
    val title: String,
    val topBar: @Composable (
        backStack: SnapshotStateList<AppRouteKey>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) -> Unit = { _, _ -> },
    val fab: @Composable (backStack: SnapshotStateList<AppRouteKey>) -> Unit = {},
)

fun AppRouteKey.chrome() = when (this) {
    is AppRouteKey.Onboarding -> onboardingChrome()
    is AppRouteKey.Chat -> chatChrome(this.chatId)
    is AppRouteKey.Settings -> settingsChrome()
}

