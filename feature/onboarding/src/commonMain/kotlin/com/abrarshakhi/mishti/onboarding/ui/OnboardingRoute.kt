package com.abrarshakhi.mishti.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.abrarshakhi.mishti.core.domain.preferences.AppPreferences
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OnboardingRoute() {
    val navigator: Navigator = koinInject()
    val preferences: AppPreferences = koinInject()
    val scope = rememberCoroutineScope()

    OnboardingScreen(onContinue = {
        scope.launch {
            preferences.setOnboardingCompleted(true)
            navigator.resetTo(Screen.Chat())
        }
    })
}
