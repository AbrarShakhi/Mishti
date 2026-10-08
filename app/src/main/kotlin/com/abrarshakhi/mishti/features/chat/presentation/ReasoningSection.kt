package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.Spacing

private val LivePreviewHeight = 120.dp
private val LivePreviewFade = 32.dp
private val RuleWidth = 2.dp
private val HeaderIconSize = 18.dp

@Composable
internal fun ReasoningSection(
    reasoning: String,
    isReasoning: Boolean,
    durationMillis: Long?,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ReasoningHeader(
            isReasoning = isReasoning,
            durationMillis = durationMillis,
            expanded = expanded,
            onToggle = onToggle,
        )

        AnimatedVisibility(
            visible = expanded && reasoning.isNotBlank(),
            enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
        ) {
            val body = Modifier
                .padding(top = Spacing.Small)
                .reasoningRule(MaterialTheme.colorScheme.outlineVariant)
            if (isReasoning) {
                LiveReasoning(text = reasoning, modifier = body)
            } else {
                SelectionContainer(modifier = body) { ReasoningText(text = reasoning) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReasoningHeader(
    isReasoning: Boolean,
    durationMillis: Long?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ReasoningChevron",
    )

    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(
                onClickLabel = stringResource(
                    if (expanded) R.string.chat_reasoning_hide else R.string.chat_reasoning_show,
                ),
                role = Role.Button,
                onClick = onToggle,
            )
            .padding(
                start = Spacing.Medium,
                end = Spacing.Small,
                top = Spacing.Small,
                bottom = Spacing.Small,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        if (isReasoning) {
            LoadingIndicator(modifier = Modifier.size(HeaderIconSize))
        } else {
            Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(HeaderIconSize),
            )
        }
        Text(
            text = reasoningLabel(isReasoning, durationMillis),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(HeaderIconSize)
                .rotate(chevronRotation),
        )
    }
}

@Composable
private fun reasoningLabel(isReasoning: Boolean, durationMillis: Long?): String = when {
    isReasoning -> stringResource(R.string.chat_reasoning_active)
    durationMillis != null -> {
        val seconds = ((durationMillis + 500) / 1000).coerceAtLeast(1).toInt()
        pluralStringResource(R.plurals.chat_reasoning_duration, seconds, seconds)
    }
    else -> stringResource(R.string.chat_reasoning_done)
}

@Composable
private fun LiveReasoning(text: String, modifier: Modifier = Modifier) {
    var contentHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = LivePreviewHeight)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (contentHeight > size.height) {
                    val fade = LivePreviewFade.toPx()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = fade,
                        ),
                        size = Size(size.width, fade),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
            .clipToBounds()
            .wrapContentHeight(align = Alignment.Bottom, unbounded = true),
    ) {
        ReasoningText(text = text, modifier = Modifier.onSizeChanged { contentHeight = it.height })
    }
}

@Composable
private fun ReasoningText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun Modifier.reasoningRule(color: Color): Modifier =
    drawBehind {
        val x = RuleWidth.toPx() / 2
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = RuleWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }.padding(start = Spacing.Medium)
