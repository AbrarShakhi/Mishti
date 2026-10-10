package com.abrarshakhi.mishti.features.splash.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

fun EntryProviderScope<AppRouteKey>.splashEntry() {
    entry<AppRouteKey.Splash> {
        SplashScreenRoute(navigator = koinInject())
    }
}

@Composable
private fun SplashScreenRoute(navigator: Navigator) {
    val viewModel: SplashViewModel = koinViewModel()
    SplashScreen(
        onFinishSplashing = { navigator.resetTo(it) },
        viewModel = viewModel
    )
}

