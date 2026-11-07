package com.abrarshakhi.mishti.splash.ui

import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashRoute() {
    val navigator: Navigator = koinInject()
    SplashScreen(
        onFinishSplashing = { navigator.resetTo(it) },
        viewModel = koinViewModel(),
    )
}
