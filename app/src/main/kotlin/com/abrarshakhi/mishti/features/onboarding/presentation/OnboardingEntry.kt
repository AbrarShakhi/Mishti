package com.abrarshakhi.mishti.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.Navigator
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

fun EntryProviderScope<AppRouteKey>.onboardingEntry(navigator: Navigator) {
    entry<AppRouteKey.Onboarding> {
        OnboardingRoute(navigator = navigator)
    }
}

@Composable
private fun OnboardingRoute(navigator: Navigator) {
    val preferences: AppPreferences = koinInject()
    val scope = rememberCoroutineScope()

    OnboardingScreen(
        onContinue = {
            scope.launch {
                preferences.setOnboardingCompleted(true)
                navigator.resetTo(AppRouteKey.Chat())
            }
        },
        modifier = Modifier,
    )
}
