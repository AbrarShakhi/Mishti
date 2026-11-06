package com.abrarshakhi.mishti.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.abrarshakhi.mishti.shared.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.dynamic.LottieDynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

/** Raw JSON plus the intro/loop ranges expressed as 0..1 progress. */
private class AnimationSource(
    val path: String,
    val json: String,
    val intro: ClosedFloatingPointRange<Float>?,
    val loop: ClosedFloatingPointRange<Float>?,
)

private suspend fun loadAnimationSource(path: String): AnimationSource =
    withContext(Dispatchers.Default) {
        val json = Res.readBytes(path).decodeToString()
        val root = Json.parseToJsonElement(json).jsonObject

        // Lottie: ip/op = in/out frame, markers[] = { cm: name, tm: start frame, dr: duration }
        val inPoint = root["ip"]?.jsonPrimitive?.floatOrNull ?: 0f
        val outPoint = root["op"]?.jsonPrimitive?.floatOrNull ?: inPoint
        val totalFrames = outPoint - inPoint
        val markers = root["markers"]?.jsonArray.orEmpty().map { it.jsonObject }

        fun clip(name: String): ClosedFloatingPointRange<Float>? {
            if (totalFrames <= 0f) return null
            val marker = markers.firstOrNull {
                it["cm"]?.jsonPrimitive?.contentOrNull == name
            } ?: return null
            val start = marker["tm"]?.jsonPrimitive?.floatOrNull ?: return null
            val end = start + (marker["dr"]?.jsonPrimitive?.floatOrNull ?: 0f)
            val from = ((start - inPoint) / totalFrames).coerceIn(0f, 1f)
            val to = ((end - inPoint) / totalFrames).coerceIn(0f, 1f)
            return if (to > from) from..to else null
        }

        AnimationSource(path, json, clip(IntroMarker), clip(LoopMarker))
    }

@Composable
internal fun OnboardingAnimation(
    animationPath: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val source by produceState<AnimationSource?>(null, animationPath) {
        value = loadAnimationSource(animationPath)
    }

    // The Box always takes the caller's size, so the layout doesn't jump when loading finishes.
    Box(modifier) {
        source?.let { AnimationPlayer(it, isActive, Modifier.fillMaxSize()) }
    }
}

@Composable
private fun AnimationPlayer(
    source: AnimationSource,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(source) {
        LottieCompositionSpec.JsonString(source.json)
    }
    val animatable = rememberLottieAnimatable()
    var introPlayed by rememberSaveable(source.path) { mutableStateOf(false) }

    LaunchedEffect(composition, isActive) {
        val loaded = composition ?: return@LaunchedEffect
        if (!isActive) return@LaunchedEffect

        if (!introPlayed) {
            source.intro?.let { intro ->
                animatable.animate(
                    loaded,
                    clipSpec = LottieClipSpec.Progress(intro.start, intro.endInclusive),
                )
            }
            introPlayed = true
        }

        val loop = source.loop ?: 0f..1f
        animatable.animate(
            loaded,
            iterations = Compottie.IterateForever,
            clipSpec = LottieClipSpec.Progress(loop.start, loop.endInclusive),
        )
    }

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { animatable.value },
            dynamicProperties = rememberThemedArt(),
        ),
        contentDescription = null,
        modifier = modifier,
    )
}

@OptIn(ExperimentalCompottieApi::class)
@Composable
private fun rememberThemedArt(): LottieDynamicProperties {
    val scheme by rememberUpdatedState(MaterialTheme.colorScheme)

    return rememberLottieDynamicProperties {
        ArtRoles.forEach { (role, pick) ->
            shapeLayer("**", role) {
                fill("**") { color { pick(scheme) } }
                stroke("**") { color { pick(scheme) } }
            }
        }
    }
}