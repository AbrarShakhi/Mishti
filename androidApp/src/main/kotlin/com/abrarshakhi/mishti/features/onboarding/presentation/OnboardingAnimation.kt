package com.abrarshakhi.mishti.features.onboarding.presentation

import androidx.annotation.RawRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

private const val IntroMarker = "intro"
private const val LoopMarker = "loop"

private val ArtRoles: List<Pair<String, (ColorScheme) -> Color>> = listOf(
    "primary" to { it.primary },
    "onPrimary" to { it.onPrimary },
    "primaryContainer" to { it.primaryContainer },
    "secondary" to { it.secondary },
    "secondaryContainer" to { it.secondaryContainer },
    "tertiary" to { it.tertiary },
    "tertiaryContainer" to { it.tertiaryContainer },
    "surface" to { it.surface },
    "onSurfaceVariant" to { it.onSurfaceVariant },
)

@Composable
internal fun OnboardingAnimation(@RawRes animation: Int, isActive: Boolean, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))
    val animatable = rememberLottieAnimatable()
    var introPlayed by rememberSaveable(animation) { mutableStateOf(false) }

    LaunchedEffect(composition, isActive) {
        val loaded = composition
        if (loaded == null || !isActive) return@LaunchedEffect

        if (!introPlayed) {
            animatable.animate(loaded, clipSpec = LottieClipSpec.Marker(IntroMarker))
            introPlayed = true
        }

        val loopStart = loaded.getMarker(LoopMarker)?.startFrame ?: loaded.startFrame
        val loopStartProgress = loaded.getProgressForFrame(loopStart)
        animatable.animate(
            loaded,
            clipSpec = LottieClipSpec.Marker(LoopMarker),
            iterations = LottieConstants.IterateForever,
            initialProgress = animatable.progress.coerceAtLeast(loopStartProgress),
        )
    }

    LottieAnimation(
        composition = composition,
        progress = { animatable.progress },
        dynamicProperties = rememberThemedArt(),
        modifier = modifier,
    )
}

@Composable
private fun rememberThemedArt(): LottieDynamicProperties {
    val scheme = MaterialTheme.colorScheme
    val properties = ArtRoles.flatMap { (role, color) ->
        val argb = color(scheme).toArgb()
        listOf(
            rememberLottieDynamicProperty(LottieProperty.COLOR, argb, role, "**"),
            rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, argb, role, "**"),
        )
    }
    return rememberLottieDynamicProperties(*properties.toTypedArray())
}
