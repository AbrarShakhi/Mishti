package com.abrarshakhi.mishti.core.designsystem.utils

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.window.core.layout.WindowSizeClass

// sealed interface DevicePosture {
//    object NormalPosture : DevicePosture
//    data class BookPosture(val hingePosition: Rect) : DevicePosture
//    data class Separating(val hingePosition: Rect, var orientation: FoldingFeature.Orientation) : DevicePosture
// }
//
// @OptIn(ExperimentalContracts::class)
// fun isBookPosture(foldFeature: FoldingFeature?): Boolean {
//    contract { returns(true) implies (foldFeature != null) }
//    return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
//            foldFeature.orientation == FoldingFeature.Orientation.VERTICAL
// }
//
// @OptIn(ExperimentalContracts::class)
// fun isSeparating(foldFeature: FoldingFeature?): Boolean {
//    contract { returns(true) implies (foldFeature != null) }
//    return foldFeature?.state == FoldingFeature.State.FLAT && foldFeature.isSeparating
// }

enum class MishtiNavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER,
}

enum class MishtiNavigationContentPosition {
    TOP,
    CENTER,
}

enum class MishtiContentType {
    SINGLE_PANE,
    DUAL_PANE,
}

@Composable
fun WindowAdaptiveInfo.getWindowContentType(): MishtiContentType {
    val hingeBounds: Rect? = windowPosture.separatingVerticalHingeBounds.firstOrNull()
        ?: windowPosture.separatingHorizontalHingeBounds.firstOrNull()

    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            MishtiContentType.DUAL_PANE

        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
            hingeBounds != null -> MishtiContentType.DUAL_PANE

        else -> MishtiContentType.SINGLE_PANE
    }
}
