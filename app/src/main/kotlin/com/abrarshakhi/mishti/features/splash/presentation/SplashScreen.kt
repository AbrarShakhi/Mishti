package com.abrarshakhi.mishti.features.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing

private const val MarkStartScale = 0.6f
private const val MarkStartRotation = -45f

private val MarkSize = 160.dp
private val TitleRise = 16.dp

@Composable
fun SplashScreen(onFinishSplashing: (AppRouteKey) -> Unit, viewModel: SplashViewModel) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()
    val entranceSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    val entrance = remember { Animatable(0f) }
    var isAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(entrance) {
        entrance.animateTo(1f, entranceSpec)
        isAnimationFinished = true
    }

    LaunchedEffect(startRoute, isAnimationFinished) {
        startRoute?.let { route ->
            if (isAnimationFinished) {
                onFinishSplashing(route)
            }
        }
    }

    SplashContent(progress = { entrance.value })
}

@Composable
private fun SplashContent(progress: () -> Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(
            size = MarkSize,
            modifier = Modifier.graphicsLayer {
                val value = progress()
                val scale = lerp(MarkStartScale, 1f, value)
                scaleX = scale
                scaleY = scale
                rotationZ = lerp(MarkStartRotation, 0f, value)
                alpha = value.coerceIn(0f, 1f)
            },
        )

        Spacer(modifier = Modifier.height(Spacing.ExtraLarge))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer {
                val value = progress()
                alpha = value.coerceIn(0f, 1f)
                translationY = lerp(TitleRise.toPx(), 0f, value)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashContentPreview() {
    MishtiTheme {
        SplashContent(progress = { 1f })
    }
}
