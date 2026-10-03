package com.abrarshakhi.mishti.common.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val SharedAxisDistance = 30.dp

/**
 * Material's shared-axis (X) transition between screens: going [forward] slides the incoming
 * screen in from the end edge while the outgoing one drifts toward the start, going back mirrors
 * it, and both cross-fade. The springs come from the theme's motion scheme.
 */
@Composable
fun rememberSharedAxisTransition(forward: Boolean): ContentTransform {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val distance = with(LocalDensity.current) { SharedAxisDistance.roundToPx() }
    val layoutDirection = LocalLayoutDirection.current

    return remember(forward, spatialSpec, effectsSpec, distance, layoutDirection) {
        val towardEnd = if (layoutDirection == LayoutDirection.Ltr) distance else -distance
        val enterOffset = if (forward) towardEnd else -towardEnd

        (slideInHorizontally(spatialSpec) { enterOffset } + fadeIn(effectsSpec)) togetherWith
            (slideOutHorizontally(spatialSpec) { -enterOffset } + fadeOut(effectsSpec))
    }
}
