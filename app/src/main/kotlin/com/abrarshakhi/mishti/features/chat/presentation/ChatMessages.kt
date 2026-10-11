package com.abrarshakhi.mishti.features.chat.presentation

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.modifiers.verticalScrollbar
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val StreamingItemKey = "streaming"
private const val MaxUserMessageWidthFraction = 0.85f
private const val PlaceholderDelayMillis = 250L
private const val FreshMessageMillis = 2_000L

private val AccentWidth = 3.dp
private val AccentContentInset = AccentWidth + Spacing.Medium
private val EntranceShift = 24.dp
private val PlaceholderHeaderWidth = 72.dp

private enum class AccentEdge { Start, End }

@Composable
internal fun MessageList(
    messages: List<ChatMessage>,
    streamingResponse: String,
    streamingReasoning: String?,
    isReasoning: Boolean,
    reasoningMillis: Long?,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val newestMessageId = messages.lastOrNull()?.id
    val newestFirst = messages.asReversed()

    LaunchedEffect(isGenerating) {
        if (listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }
    LaunchedEffect(newestMessageId) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .verticalScrollbar(listState, color = MaterialTheme.colorScheme.outlineVariant),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = Spacing.ScreenMargin, vertical = Spacing.Large),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small, Alignment.Bottom),
    ) {
        if (isGenerating) {
            item(key = StreamingItemKey) {
                AssistantMessage(
                    text = streamingResponse,
                    reasoning = streamingReasoning,
                    isReasoning = isReasoning,
                    reasoningMillis = reasoningMillis,
                    tokensPerSecond = null,
                    isStreaming = true,
                    showHeader = messages.lastOrNull()?.author != MessageAuthor.Assistant,
                    animateEntrance = true,
                )
            }
        }

        itemsIndexed(items = newestFirst, key = { _, message -> message.id }) { index, message ->
            val showHeader = newestFirst.getOrNull(index + 1)?.author != message.author
            when (message.author) {
                MessageAuthor.User -> UserMessage(
                    text = message.content,
                    createdAtMillis = message.createdAtMillis,
                    showHeader = showHeader,
                    animateEntrance = message.isFresh(),
                )

                MessageAuthor.Assistant -> AssistantMessage(
                    text = message.content,
                    reasoning = message.reasoning,
                    isReasoning = false,
                    reasoningMillis = message.reasoningMillis,
                    tokensPerSecond = message.tokensPerSecond,
                    isStreaming = false,
                    createdAtMillis = message.createdAtMillis,
                    showHeader = showHeader,
                )
            }
        }
    }
}

@Composable
private fun UserMessage(
    text: String,
    modifier: Modifier = Modifier,
    createdAtMillis: Long? = null,
    showHeader: Boolean = true,
    animateEntrance: Boolean = false,
) {
    val accent = MaterialTheme.colorScheme.primary
    val entrance by rememberEntranceProgress(animateEntrance)

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        if (showHeader) {
            MessageHeader(
                name = stringResource(R.string.chat_sender_user),
                color = accent,
                createdAtMillis = createdAtMillis,
                edge = AccentEdge.End,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(MaxUserMessageWidthFraction)
                .wrapContentWidth(Alignment.End)
                .accentBar(accent, AccentEdge.End) { entrance }
                .accentInset(AccentEdge.End)
                .entranceShift(AccentEdge.End) { entrance },
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AssistantMessage(
    text: String,
    reasoning: String?,
    isReasoning: Boolean,
    reasoningMillis: Long?,
    tokensPerSecond: Double?,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    createdAtMillis: Long? = null,
    showHeader: Boolean = true,
    animateEntrance: Boolean = false,
) {
    val accent = MaterialTheme.colorScheme.tertiary
    val entrance by rememberEntranceProgress(animateEntrance)

    Column(modifier = modifier.fillMaxWidth()) {
        if (showHeader) {
            MessageHeader(
                name = stringResource(R.string.chat_sender_assistant),
                color = accent,
                createdAtMillis = createdAtMillis,
                edge = AccentEdge.Start,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .accentBar(accent, AccentEdge.Start) { entrance }
                .accentInset(AccentEdge.Start)
                .entranceShift(AccentEdge.Start) { entrance },
        ) {
            if (reasoning != null) {
                var expanded by rememberSaveable { mutableStateOf(false) }
                if (isStreaming) {
                    LaunchedEffect(isReasoning) { expanded = isReasoning }
                }
                ReasoningSection(
                    reasoning = reasoning,
                    isReasoning = isReasoning,
                    durationMillis = reasoningMillis,
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                    modifier = Modifier.padding(bottom = Spacing.Small),
                )
            }

            SelectionContainer {
                if (isStreaming) {
                    key(reasoning != null) {
                        StreamingMarkdownReply(
                            markdown = text,
                            placeholder = {
                                if (reasoning == null) {
                                    DisableSelection {
                                        ReadingIndicator()
                                    }
                                }
                            },
                        )
                    }
                } else if (text.isNotEmpty()) {
                    MarkdownReply(markdown = text)
                }
            }
        }

        if (!isStreaming) {
            MessageActions(
                text = text,
                tokensPerSecond = tokensPerSecond,
                modifier = Modifier.accentInset(AccentEdge.Start),
            )
        }
    }
}

@Composable
private fun MessageHeader(name: String, color: Color, createdAtMillis: Long?, edge: AccentEdge) {
    val time = createdAtMillis?.let { rememberMessageTime(it) }

    Row(
        modifier = Modifier.padding(top = Spacing.Large, bottom = Spacing.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        if (edge == AccentEdge.End && time != null) {
            MessageTime(time)
        }
        Text(
            text = name.toUpperCase(Locale.current),
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = color,
            maxLines = 1,
        )
        if (edge == AccentEdge.Start && time != null) {
            MessageTime(time)
        }
    }
}

@Composable
private fun MessageTime(time: String) {
    Text(
        text = time,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReadingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        LoadingIndicator(modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(R.string.chat_reading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MessageActions(text: String, tokensPerSecond: Double?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.offset(x = -Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text.isNotBlank()) {
            CopyButton(text = text, contentDescription = stringResource(R.string.chat_copy_reply))
        }

        if (tokensPerSecond != null) {
            Text(
                text = stringResource(
                    R.string.chat_tokens_per_second,
                    "%.1f".format(tokensPerSecond),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberEntranceProgress(animate: Boolean): State<Float> {
    val spec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    var played by rememberSaveable { mutableStateOf(!animate) }
    val progress = remember { Animatable(if (played) 1f else 0f) }

    LaunchedEffect(progress) {
        if (!played) {
            played = true
            progress.animateTo(1f, spec)
        }
    }
    return progress.asState()
}

@Composable
private fun rememberMessageTime(createdAtMillis: Long): String {
    val context = LocalContext.current
    return remember(context, createdAtMillis) { formatMessageTime(context, createdAtMillis) }
}

private fun formatMessageTime(context: Context, millis: Long): String {
    val flags = if (DateUtils.isToday(millis)) {
        DateUtils.FORMAT_SHOW_TIME
    } else {
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
    }
    return DateUtils.formatDateTime(context, millis, flags)
}

private fun ChatMessage.isFresh(): Boolean = System.currentTimeMillis() - createdAtMillis in 0..FreshMessageMillis

private fun Modifier.accentBar(color: Color, edge: AccentEdge, progress: () -> Float): Modifier = drawBehind {
    val width = AccentWidth.toPx()
    val height = size.height * progress().coerceIn(0f, 1f)
    val onLeft = (edge == AccentEdge.Start) == (layoutDirection == LayoutDirection.Ltr)
    drawRoundRect(
        color = color,
        topLeft = Offset(if (onLeft) 0f else size.width - width, size.height - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2),
    )
}

private fun Modifier.accentInset(edge: AccentEdge): Modifier = when (edge) {
    AccentEdge.Start -> padding(start = AccentContentInset)
    AccentEdge.End -> padding(end = AccentContentInset)
}

private fun Modifier.entranceShift(edge: AccentEdge, progress: () -> Float): Modifier = offset {
    val distance = (1f - progress()) * EntranceShift.toPx()
    val towardAccent = if (edge == AccentEdge.Start) -distance else distance
    IntOffset(towardAccent.roundToInt(), distance.roundToInt())
}

@Composable
internal fun ConversationPlaceholder(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(PlaceholderDelayMillis.milliseconds)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(MaterialTheme.motionScheme.slowEffectsSpec()),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shimmer(rememberShimmer(ShimmerBounds.View))
                .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small, Alignment.Bottom),
        ) {
            PlaceholderMessage(edge = AccentEdge.End, lineFractions = listOf(0.55f))
            PlaceholderMessage(edge = AccentEdge.Start, lineFractions = listOf(1f, 0.92f, 0.6f))
            PlaceholderMessage(edge = AccentEdge.End, lineFractions = listOf(0.4f))
            PlaceholderMessage(edge = AccentEdge.Start, lineFractions = listOf(1f, 0.7f))
        }
    }
}

@Composable
private fun PlaceholderMessage(edge: AccentEdge, lineFractions: List<Float>) {
    val bone = MaterialTheme.colorScheme.surfaceContainerHighest
    val alignment = if (edge == AccentEdge.Start) Alignment.Start else Alignment.End

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .padding(top = Spacing.Large, bottom = Spacing.Small)
                .width(PlaceholderHeaderWidth)
                .height(12.dp)
                .background(bone, CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(if (edge == AccentEdge.Start) 1f else MaxUserMessageWidthFraction)
                .accentBar(bone, edge) { 1f }
                .accentInset(edge)
                .padding(vertical = Spacing.ExtraSmall),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            lineFractions.forEach { fraction ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(14.dp)
                        .background(bone, CircleShape),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AssistantMessagePreview() {
    AssistantMessage(
        text = "this is an example text",
        reasoning = null,
        isReasoning = false,
        reasoningMillis = null,
        tokensPerSecond = 10.0,
        isStreaming = false,
    )
}
