package com.abrarshakhi.mishti.features.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.BreakIterator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private val ClusterSize = 228.dp
private val CookieSize = 132.dp
private val WordmarkGap = 28.dp
private val LetterRise = 22.dp
private val RingWidth = 2.5.dp
private val SprinkleLength = 14.dp
private val SprinkleThickness = 5.dp

private const val CookieStartScale = 0.3f
private const val CookieStartTurn = -120f
private const val ArtStartScale = 2.2f
private const val LetterStartTilt = -10f
private const val SprinkleCount = 12
private const val SettleFrames = 2
private const val SprinkleSpin = 140f

private val RingDelay = 60.milliseconds
private val SprinkleDelay = 120.milliseconds
private val LiftDelay = 150.milliseconds
private val WordmarkDelay = 180.milliseconds
private val LetterStagger = 32.milliseconds

private val SprinkleTones = listOf(
    Color(0xFFFF8FA2),
    Color(0xFFFFAA2B),
    Color(0xFF86D05F),
    Color(0xFFFFD54F),
)

private data class Sprinkle(val angle: Float, val reach: Float, val capsule: Boolean, val tone: Int)

private val Sprinkles = List(SprinkleCount) { index ->
    Sprinkle(
        angle = index * 360f / SprinkleCount + ((index * 37) % 11 - 5) * 2f,
        reach = 0.82f + ((index * 53) % 7) * 0.05f,
        capsule = index % 2 == 0,
        tone = index % SprinkleTones.size,
    )
}

@Stable
private class SplashMotion(letterCount: Int, settled: Boolean) {
    private val start = if (settled) 1f else 0f

    private fun track() = Animatable(start)

    val glow = track()
    val bloom = track()
    val morph = track()
    val turn = track()
    val ring = track()
    val art = track()
    val lift = track()
    val sprinkles = track()
    val letters = List(letterCount) { track() }

    suspend fun play(scheme: MotionScheme) = coroutineScope {
        val bouncy = scheme.fastSpatialSpec<Float>()
        launch { glow.settle(scheme.slowEffectsSpec()) }
        launch { bloom.settle(bouncy) }
        launch { morph.settle(scheme.defaultSpatialSpec()) }
        launch { turn.settle(scheme.slowSpatialSpec()) }
        launch {
            delay(RingDelay)
            ring.settle(scheme.slowSpatialSpec())
        }
        launch { art.settle(scheme.slowSpatialSpec()) }
        launch {
            delay(LiftDelay)
            lift.settle(scheme.defaultSpatialSpec())
        }
        launch {
            delay(SprinkleDelay)
            sprinkles.settle(scheme.slowSpatialSpec())
        }
        letters.forEachIndexed { index, letter ->
            launch {
                delay(WordmarkDelay + LetterStagger * index)
                letter.settle(bouncy)
            }
        }
    }

    private suspend fun Animatable<Float, AnimationVector1D>.settle(spec: FiniteAnimationSpec<Float>) {
        animateTo(1f, spec)
    }
}

@Composable
fun SplashScreen(onFinishSplashing: (AppRouteKey) -> Unit, viewModel: SplashViewModel) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()
    val letters = graphemes(stringResource(R.string.app_name))
    val motion = remember(letters.size) { SplashMotion(letters.size, settled = false) }
    val scheme = MaterialTheme.motionScheme
    var isAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(motion) {
        repeat(SettleFrames) { withFrameNanos { } }
        motion.play(scheme)
        isAnimationFinished = true
    }

    LaunchedEffect(startRoute, isAnimationFinished) {
        startRoute?.let { route ->
            if (isAnimationFinished) {
                onFinishSplashing(route)
            }
        }
    }

    SplashContent(motion = motion, letters = letters)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SplashContent(motion: SplashMotion, letters: List<String>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val container = colors.primaryContainer
    val ringColor = colors.primary
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val cookiePath = remember { Path() }

    var wordmarkHeight by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
            .graphicsLayer {
                val centred = (WordmarkGap.toPx() + wordmarkHeight) / 2f
                translationY = lerp(centred, 0f, motion.lift.value)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(ClusterSize), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cookie = CookieSize.toPx()
                drawGlow(container, cookie, motion.glow.value)
                drawRing(ringColor, cookie, motion.ring.value)
                drawSprinkles(SprinkleTones, cookie, motion.sprinkles.value)
                drawCookie(morph, cookiePath, container, cookie, motion)
            }
            AppMark(
                size = CookieSize,
                containerColor = Color.Transparent,
                modifier = Modifier.graphicsLayer {
                    val scale = lerp(ArtStartScale, 1f, motion.art.value)
                    scaleX = scale
                    scaleY = scale
                },
            )
        }

        Spacer(modifier = Modifier.height(WordmarkGap))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.onSizeChanged { wordmarkHeight = it.height },
        ) {
            letters.forEachIndexed { index, letter ->
                val track = motion.letters[index]
                Text(
                    text = letter,
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    color = colors.primary,
                    modifier = Modifier.graphicsLayer {
                        val value = track.value
                        alpha = value.coerceIn(0f, 1f)
                        translationY = lerp(LetterRise.toPx(), 0f, value)
                        rotationZ = lerp(LetterStartTilt, 0f, value)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                )
            }
        }
    }
}

private fun DrawScope.drawGlow(color: Color, cookie: Float, progress: Float) {
    if (progress <= 0f) return
    val radius = cookie * lerp(0.4f, 0.95f, progress)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f * progress.coerceAtMost(1f)), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
    )
}

private fun DrawScope.drawRing(color: Color, cookie: Float, progress: Float) {
    if (progress <= 0f || progress >= 1f) return
    drawCircle(
        color = color.copy(alpha = 0.45f * (1f - progress)),
        radius = lerp(cookie * 0.5f, size.minDimension * 0.5f, progress),
        style = Stroke(width = RingWidth.toPx()),
    )
}

private fun DrawScope.drawSprinkles(tones: List<Color>, cookie: Float, progress: Float) {
    if (progress <= 0f) return
    val grow = (progress / 0.3f).coerceIn(0f, 1f)
    val shrink = ((progress - 0.55f) / 0.45f).coerceIn(0f, 1f)
    val scale = lerp(0.3f, 1f, grow) * (1f - shrink)
    if (scale <= 0f) return
    val alpha = 1f - ((progress - 0.75f) / 0.25f).coerceIn(0f, 1f)
    val length = SprinkleLength.toPx() * scale
    val thickness = SprinkleThickness.toPx() * scale
    val start = cookie * 0.32f
    val end = size.minDimension * 0.5f
    Sprinkles.forEach { sprinkle ->
        val radians = sprinkle.angle * PI.toFloat() / 180f
        val distance = lerp(start, end * sprinkle.reach, progress)
        val color = tones[sprinkle.tone].copy(alpha = alpha)
        translate(center.x + cos(radians) * distance, center.y + sin(radians) * distance) {
            rotate(sprinkle.angle + SprinkleSpin * progress, pivot = Offset.Zero) {
                if (sprinkle.capsule) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(-length / 2f, -thickness / 2f),
                        size = Size(length, thickness),
                        cornerRadius = CornerRadius(thickness / 2f),
                    )
                } else {
                    drawCircle(color = color, radius = thickness * 0.7f, center = Offset.Zero)
                }
            }
        }
    }
}

private fun DrawScope.drawCookie(morph: Morph, path: Path, color: Color, cookie: Float, motion: SplashMotion) {
    val bloom = motion.bloom.value
    if (bloom <= 0f) return
    path.rewind()
    morph.asCubics(motion.morph.value).forEachIndexed { index, cubic ->
        if (index == 0) path.moveTo(cubic.anchor0X * cookie, cubic.anchor0Y * cookie)
        path.cubicTo(
            cubic.control0X * cookie,
            cubic.control0Y * cookie,
            cubic.control1X * cookie,
            cubic.control1Y * cookie,
            cubic.anchor1X * cookie,
            cubic.anchor1Y * cookie,
        )
    }
    path.close()
    val offset = (size.minDimension - cookie) / 2f
    rotate(lerp(CookieStartTurn, 0f, motion.turn.value)) {
        scale(lerp(CookieStartScale, 1f, bloom)) {
            translate(offset, offset) {
                drawPath(path = path, color = color)
            }
        }
    }
}

private fun graphemes(text: String): List<String> {
    val iterator = BreakIterator.getCharacterInstance().apply { setText(text) }
    val pieces = mutableListOf<String>()
    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        pieces += text.substring(start, end)
        start = end
        end = iterator.next()
    }
    return pieces
}

@Preview(showBackground = true)
@Composable
private fun SplashContentPreview() {
    MishtiTheme {
        val letters = graphemes(stringResource(R.string.app_name))
        SplashContent(motion = remember { SplashMotion(letters.size, settled = true) }, letters = letters)
    }
}
