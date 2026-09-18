package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ChatScreen(
    state: ChatUiState,
    onIntent: (ChatIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading -> LoadingConversation(
                    modifier = Modifier.align(Alignment.Center),
                )

                state.messages.isEmpty() && !state.isGenerating -> EmptyConversation(
                    engineState = state.engineState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                )

                else -> MessageList(
                    messages = state.messages,
                    streamingResponse = state.streamingResponse,
                    isGenerating = state.isGenerating,
                )
            }
        }

        HorizontalDivider()

        MessageComposer(
            draft = state.draft,
            canSend = state.canSend,
            canStop = state.canStop,
            onDraftChanged = { onIntent(ChatIntent.DraftChanged(it)) },
            onSend = { onIntent(ChatIntent.SendClicked) },
            onStop = { onIntent(ChatIntent.StopClicked) },
        )
    }
}


private const val LoadingIndicatorDelayMillis = 250L


@Composable
private fun LoadingConversation(modifier: Modifier = Modifier) {
    var showIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(LoadingIndicatorDelayMillis.milliseconds)
        showIndicator = true
    }

    if (showIndicator) {
        CircularProgressIndicator(
            modifier = modifier,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyConversation(
    engineState: EngineState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Start a conversation",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = when (engineState) {
                is EngineState.Ready -> "Running ${engineState.model.name}. Messages stay on this device."
                is EngineState.Loading -> "Preparing ${engineState.model.name}…"
                is EngineState.Failed -> "The model could not be loaded."
                EngineState.Idle -> "No model selected. Choose one in Settings › Models."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    streamingResponse: String,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val itemCount = messages.size + if (isGenerating) 1 else 0
    LaunchedEffect(messages.lastOrNull()?.id, streamingResponse, isGenerating) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = messages, key = { it.id }) { message ->
            MessageBubble(
                text = message.content,
                isUser = message.author == MessageAuthor.User,
                tokensPerSecond = message.tokensPerSecond,
            )
        }

        if (isGenerating) {
            item(key = "streaming") {
                MessageBubble(
                    text = streamingResponse.ifEmpty { "…" },
                    isUser = false,
                    tokensPerSecond = null,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    isUser: Boolean,
    tokensPerSecond: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isUser) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }

        if (tokensPerSecond != null) {
            Text(
                text = "%.1f tok/s".format(tokensPerSecond),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }
}

@Composable
private fun MessageComposer(
    draft: String,
    canSend: Boolean,
    canStop: Boolean,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Mishti") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
            )
            FilledIconButton(
                onClick = if (canStop) onStop else onSend,
                enabled = canStop || canSend,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Icon(
                    imageVector = if (canStop) {
                        Icons.Filled.Close
                    } else {
                        Icons.AutoMirrored.Filled.Send
                    },
                    contentDescription = if (canStop) "Stop generating" else "Send message",
                )
            }
        }
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun ChatScreenEmptyPreview() {
    MishtiTheme {
        ChatScreen(state = ChatUiState(isLoading = false), onIntent = {})
    }
}

@Preview(name = "With messages", showBackground = true)
@Composable
private fun ChatScreenWithMessagesPreview() {
    MishtiTheme {
        ChatScreen(
            state = ChatUiState(
                isLoading = false,
                messages = listOf(
                    ChatMessage("1", MessageAuthor.User, "Hello", 0L),
                    ChatMessage("2", MessageAuthor.Assistant, "Hi — how can I help?", 0L, 12.4),
                ),
                draft = "Typing a reply",
                canSend = true,
                engineState = EngineState.Ready(
                    ModelHandle(
                        "preview", "Qwen2.5 0.5B", "/tmp/preview.gguf"
                    )
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun ChatScreenLoadingPreview() {
    MishtiTheme {
        ChatScreen(state = ChatUiState(isLoading = true), onIntent = {})
    }
}

@Preview(name = "Streaming", showBackground = true)
@Composable
private fun ChatScreenStreamingPreview() {
    MishtiTheme {
        ChatScreen(
            state = ChatUiState(
                isLoading = false,
                messages = listOf(ChatMessage("1", MessageAuthor.User, "Hello", 0L)),
                streamingResponse = "Thinking this through as tokens arri",
                isGenerating = true,
                canStop = true,
                engineState = EngineState.Ready(
                    ModelHandle(
                        "preview", "Qwen2.5 0.5B", "/tmp/preview.gguf"
                    )
                ),
            ),
            onIntent = {},
        )
    }
}
