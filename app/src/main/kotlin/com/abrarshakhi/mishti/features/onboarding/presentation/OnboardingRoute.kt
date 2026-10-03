package com.abrarshakhi.mishti.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences: AppPreferences = koinInject()
    val scope = rememberCoroutineScope()

    OnboardingScreen(
        onContinue = {
            scope.launch {
                preferences.setOnboardingCompleted(true)
                onFinished()
            }
        },
        modifier = modifier,
    )
}
