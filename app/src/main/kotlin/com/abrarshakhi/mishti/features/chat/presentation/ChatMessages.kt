package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.CookieShape
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val StreamingItemKey = "streaming"
private const val MaxBubbleWidthFraction = 0.85f
private const val PlaceholderDelayMillis = 250L

private val AvatarSize = 32.dp

/** A user's bubble, rounded everywhere but the corner that points at the composer. */
private val UserBubbleShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomEnd = 6.dp,
    bottomStart = 24.dp,
)

@Composable
internal fun MessageList(
    messages: List<ChatMessage>,
    streamingResponse: String,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val newestMessageId = messages.lastOrNull()?.id

    // The list is laid out bottom-up, so a reply that is still streaming grows upward with no
    // scrolling needed. A new turn only pulls the list down if the reader was already at the end.
    LaunchedEffect(newestMessageId, isGenerating) {
        if (listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = Spacing.ScreenMargin, vertical = Spacing.Large),
        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge, Alignment.Bottom),
    ) {
        // No per-item placement animation: new turns are revealed by the scroll above, and the
        // streaming reply hands over to the stored message under a new key, so animating items
        // here would overlap or flicker.
        if (isGenerating) {
            item(key = StreamingItemKey) {
                AssistantMessage(
                    text = streamingResponse,
                    tokensPerSecond = null,
                    isStreaming = true,
                )
            }
        }

        items(items = messages.asReversed(), key = { it.id }) { message ->
            when (message.author) {
                MessageAuthor.User -> UserMessage(text = message.content)
                MessageAuthor.Assistant -> AssistantMessage(
                    text = message.content,
                    tokensPerSecond = message.tokensPerSecond,
                    isStreaming = false,
                )
            }
        }
    }
}

@Composable
private fun UserMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(MaxBubbleWidthFraction)
                .wrapContentWidth(Alignment.End),
            shape = UserBubbleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = Spacing.Large, vertical = Spacing.Medium),
                )
            }
        }
    }
}

/** A reply: Mishti's mark beside its text, rendered as Markdown, as in Material chat UIs. */
@Composable
private fun AssistantMessage(
    text: String,
    tokensPerSecond: Double?,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        AppMark(size = AvatarSize)

        Column(modifier = Modifier.weight(1f)) {
            SelectionContainer {
                if (isStreaming) {
                    StreamingMarkdownReply(
                        markdown = text,
                        placeholder = {
                            DisableSelection {
                                // Offset like the reply's first line, to centre it on the avatar.
                                ThinkingIndicator(modifier = Modifier.padding(top = Spacing.ExtraSmall))
                            }
                        },
                    )
                } else {
                    MarkdownReply(markdown = text)
                }
            }

            if (!isStreaming) {
                MessageActions(text = text, tokensPerSecond = tokensPerSecond)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThinkingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        LoadingIndicator(modifier = Modifier.size(24.dp))
        Text(
            text = "Thinking…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MessageActions(text: String, tokensPerSecond: Double?) {
    // Pulled back by the icon button's inner padding, so the glyph lines up with the text above.
    Row(
        modifier = Modifier.offset(x = -Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The Markdown as written, so pasting keeps the reply's structure.
        CopyButton(text = text, contentDescription = "Copy reply")

        if (tokensPerSecond != null) {
            Text(
                text = "%.1f tokens/s".format(tokensPerSecond),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Shimmering bones in the shape of a conversation, shown while one opens. It waits a moment
 * first, so a conversation that opens quickly never flashes a skeleton.
 */
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
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge, Alignment.Bottom),
        ) {
            PlaceholderUserBubble(widthFraction = 0.55f)
            PlaceholderReply(lineFractions = listOf(1f, 0.92f, 0.6f))
            PlaceholderUserBubble(widthFraction = 0.4f)
            PlaceholderReply(lineFractions = listOf(1f, 0.7f))
        }
    }
}

@Composable
private fun PlaceholderUserBubble(widthFraction: Float) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, UserBubbleShape),
        )
    }
}

@Composable
private fun PlaceholderReply(lineFractions: List<Float>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CookieShape),
        )
        Column(
            modifier = Modifier.weight(1f).padding(top = Spacing.ExtraSmall),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            lineFractions.forEach { fraction ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                )
            }
        }
    }
}
