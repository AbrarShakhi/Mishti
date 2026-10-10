package com.abrarshakhi.mishti.features.splash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Placeholder splash screen for now.
 * New splash screen will be available when new logo is finished.
 */
@Composable
fun SplashScreen(onFinishSplashing: (AppRouteKey) -> Unit, viewModel: SplashViewModel) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()

//    val composition by rememberLottieComposition(
//        spec = LottieCompositionSpec.RawRes(R.raw.splash_animation)
//    )
//    val progress by animateLottieCompositionAsState(
//        composition = composition,
//        iterations = 1
//    )

    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val finishAt = 100
        for (i in 1..finishAt) {
            delay(10.milliseconds)
            progress = (i / 100f)
        }
    }
    val isAnimationFinished = progress == 1f

    LaunchedEffect(startRoute, isAnimationFinished) {
        startRoute?.let { route ->
            if (isAnimationFinished) {
                onFinishSplashing(route)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        LottieAnimation(
//            composition = composition,
//            progress = { progress },
//            modifier = Modifier.fillMaxWidth()
//        )
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}