package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor

@Composable
fun ChatScreen(
    state: ChatUiState,
    onIntent: (ChatIntent) -> Unit,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenModels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatTopBar(
                title = state.title,
                engineState = state.engineState,
                scrollBehavior = scrollBehavior,
                onOpenDrawer = onOpenDrawer,
                onNewChat = onNewChat,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            Crossfade(
                targetState = ChatContent.of(state),
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "ChatContent",
            ) { content ->
                when (content) {
                    ChatContent.Loading -> ConversationPlaceholder()
                    ChatContent.Empty -> ChatEmptyState(
                        engineState = state.engineState,
                        onSuggestion = { onIntent(ChatIntent.DraftChanged(it)) },
                        onOpenModels = onOpenModels,
                    )
                    ChatContent.Conversation -> MessageList(
                        messages = state.messages,
                        streamingResponse = state.streamingResponse,
                        streamingReasoning = state.streamingReasoning,
                        isReasoning = state.isReasoning,
                        reasoningMillis = state.reasoningMillis,
                        isGenerating = state.isGenerating,
                    )
                }
            }

            ChatComposer(
                draft = state.draft,
                canSend = state.canSend,
                canStop = state.canStop,
                thinkingSupported = state.thinkingSupported,
                thinkingEnabled = state.thinkingEnabled,
                onDraftChange = { onIntent(ChatIntent.DraftChanged(it)) },
                onSend = { onIntent(ChatIntent.SendClicked) },
                onStop = { onIntent(ChatIntent.StopClicked) },
                onThinkingToggle = { onIntent(ChatIntent.ThinkingToggled) },
            )
        }
    }
}

private enum class ChatContent {
    Loading, Empty, Conversation;

    companion object {
        fun of(state: ChatUiState) = when {
            state.isLoading -> Loading
            state.messages.isEmpty() && !state.isGenerating -> Empty
            else -> Conversation
        }
    }
}

private val PreviewModel = ModelHandle("preview", "Qwen2.5 0.5B Instruct", "/tmp/preview.gguf")

@Composable
private fun ChatScreenPreview(state: ChatUiState) {
    MishtiTheme {
        ChatScreen(
            state = state,
            onIntent = {},
            onOpenDrawer = {},
            onNewChat = {},
            onOpenModels = {},
        )
    }
}

@Preview(name = "Ready, empty", showBackground = true)
@Composable
private fun ChatScreenReadyPreview() = ChatScreenPreview(
    ChatUiState(title = "New chat", isLoading = false, engineState = EngineState.Ready(PreviewModel)),
)

@Preview(name = "No model", showBackground = true)
@Composable
private fun ChatScreenNoModelPreview() = ChatScreenPreview(
    ChatUiState(title = "New chat", isLoading = false),
)

@Preview(name = "Conversation", showBackground = true)
@Composable
private fun ChatScreenConversationPreview() = ChatScreenPreview(
    ChatUiState(
        title = "Rainbows",
        isLoading = false,
        messages = listOf(
            ChatMessage("1", MessageAuthor.User, "Explain how rainbows form", 0L),
            ChatMessage(
                "2",
                MessageAuthor.Assistant,
                "Sunlight bends as it enters a raindrop, reflects off the back, and bends " +
                    "again on the way out. Each colour bends by a slightly different amount, " +
                    "so the white light fans out into a band of colours.",
                0L,
                12.4,
            ),
        ),
        draft = "Why is the sky blue?",
        canSend = true,
        engineState = EngineState.Ready(PreviewModel),
    ),
)

@Preview(name = "Streaming", showBackground = true)
@Composable
private fun ChatScreenStreamingPreview() = ChatScreenPreview(
    ChatUiState(
        title = "Rainbows",
        isLoading = false,
        messages = listOf(ChatMessage("1", MessageAuthor.User, "Explain how rainbows form", 0L)),
        streamingResponse = "Sunlight bends as it enters a rain",
        isGenerating = true,
        canStop = true,
        engineState = EngineState.Ready(PreviewModel),
    ),
)

@Preview(name = "Thinking", showBackground = true)
@Composable
private fun ChatScreenThinkingPreview() = ChatScreenPreview(
    ChatUiState(
        title = "Rainbows",
        isLoading = false,
        messages = listOf(ChatMessage("1", MessageAuthor.User, "Explain how rainbows form", 0L)),
        streamingReasoning = "The user wants the physics of rainbows. Start with refraction, " +
            "then reflection inside the drop, then dispersion.",
        isReasoning = true,
        isGenerating = true,
        canStop = true,
        engineState = EngineState.Ready(PreviewModel, supportsThinking = true),
        thinkingSupported = true,
        thinkingEnabled = true,
    ),
)

@Preview(name = "Reply with thoughts", showBackground = true)
@Composable
private fun ChatScreenReasonedPreview() = ChatScreenPreview(
    ChatUiState(
        title = "Rainbows",
        isLoading = false,
        messages = listOf(
            ChatMessage("1", MessageAuthor.User, "Explain how rainbows form", 0L),
            ChatMessage(
                id = "2",
                author = MessageAuthor.Assistant,
                content = "Sunlight bends as it enters a raindrop, reflects off the back, and " +
                    "splits into colours on the way out.",
                createdAtMillis = 0L,
                tokensPerSecond = 9.8,
                reasoning = "Refraction, reflection, dispersion. Keep it short.",
                reasoningMillis = 7_400L,
            ),
        ),
        engineState = EngineState.Ready(PreviewModel, supportsThinking = true),
        thinkingSupported = true,
    ),
)

@Preview(name = "Loading", showBackground = true)
@Composable
private fun ChatScreenLoadingPreview() = ChatScreenPreview(ChatUiState(isLoading = true))
