package com.abrarshakhi.mishti.common.main

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.features.chat.presentation.chatChrome
import com.abrarshakhi.mishti.features.models.presentation.modelsChrome
import com.abrarshakhi.mishti.features.onboarding.presentation.onboardingChrome
import com.abrarshakhi.mishti.features.settings.presentation.settingsChrome

data class ChromeScope(
    val backStack: SnapshotStateList<AppRouteKey>,
    val scrollBehavior: TopAppBarScrollBehavior,
    val openDrawer: () -> Unit,
)

data class ScreenChrome(
    val title: String,
    val topBar: @Composable (ChromeScope) -> Unit = {},
    val fab: @Composable (ChromeScope) -> Unit = {},
)

fun AppRouteKey.chrome() = when (this) {
    is AppRouteKey.Onboarding -> onboardingChrome()
    is AppRouteKey.Chat -> chatChrome()
    is AppRouteKey.Settings -> settingsChrome()
    is AppRouteKey.Models -> modelsChrome()
}
