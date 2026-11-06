package com.abrarshakhi.mishti.chat.ui

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
                engineState = state.engineState,
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
    Loading,
    Empty,
    Conversation,
    ;

    companion object {
        fun of(state: ChatUiState) = when {
            state.isLoading -> Loading
            state.messages.isEmpty() && !state.isGenerating -> Empty
            else -> Conversation
        }
    }
}
