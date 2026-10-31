package com.abrarshakhi.mishti.common.ui.modifiers

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.4f),
): Modifier = this.drawWithContent {
    drawContent()

    val layoutInfo = state.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    if (totalItemsCount == 0) return@drawWithContent

    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    if (visibleItemsInfo.isEmpty()) return@drawWithContent

    val minThumbHeight = 36f
    val viewportHeight = size.height

    val elementHeight = viewportHeight / totalItemsCount
    val scrollbarHeight = (visibleItemsInfo.size * elementHeight).coerceAtLeast(minThumbHeight)

    val scrollOffset = (
        state.firstVisibleItemIndex.toFloat() +
            (
                state.firstVisibleItemScrollOffset.toFloat() / (
                    visibleItemsInfo.first().size.takeIf { it > 0 }
                        ?: 1
                    )
                )
        )

    val scrollProgress =
        scrollOffset / (totalItemsCount - visibleItemsInfo.size.toFloat()).coerceAtLeast(1f)
    val scrollbarOffsetY = (1f - scrollProgress) * (viewportHeight - scrollbarHeight)

    drawRoundRect(
        color = color,
        topLeft = Offset(x = size.width - width.toPx() - 4.dp.toPx(), y = scrollbarOffsetY),
        size = Size(width = width.toPx(), height = scrollbarHeight),
        cornerRadius = CornerRadius(width.toPx() / 2),
    )
}
