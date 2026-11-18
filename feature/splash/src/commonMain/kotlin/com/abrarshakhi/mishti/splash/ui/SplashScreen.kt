package com.abrarshakhi.mishti.splash.ui

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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.core.designsystem.resources.app_name
import com.abrarshakhi.mishti.core.ui.components.AppMark
import com.abrarshakhi.mishti.core.ui.utils.Screen
import com.abrarshakhi.mishti.splash.ui.components.SplashContent
import com.abrarshakhi.mishti.splash.ui.components.SplashMotion
import com.abrarshakhi.mishti.splash.ui.utils.graphemes
import com.abrarshakhi.mishti.splash.ui.viewmodels.SplashViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes

private const val SettleFrames = 2

@Composable
fun SplashScreen(onFinishSplashing: (Screen) -> Unit, viewModel: SplashViewModel) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()
    val letters = graphemes(stringResource(CoreDesignsystemRes.string.app_name))
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
