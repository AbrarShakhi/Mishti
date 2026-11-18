package com.abrarshakhi.mishti.chat.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.abrarshakhi.mishti.chat.ui.components.AddOptionsModalSheet
import com.abrarshakhi.mishti.chat.ui.components.ChatComposer
import com.abrarshakhi.mishti.chat.ui.components.ChatEmptyState
import com.abrarshakhi.mishti.chat.ui.components.ChatTopBar
import com.abrarshakhi.mishti.chat.ui.components.ConversationPlaceholder
import com.abrarshakhi.mishti.chat.ui.components.MessageList
import com.abrarshakhi.mishti.chat.ui.components.ModelSelectorModalSheet
import com.abrarshakhi.mishti.chat.ui.viewmodels.contracts.ChatIntent
import com.abrarshakhi.mishti.chat.ui.viewmodels.contracts.ChatUiState

@OptIn(ExperimentalMaterial3Api::class)
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
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showModelSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatTopBar(
                scrollBehavior = scrollBehavior,
                onOpenDrawer = onOpenDrawer,
                onNewChat = onNewChat,
                sessionTitle = state.title,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
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
                canSend = state.canSend && !state.voiceInput.isActive,
                canStop = state.canStop,
                activeModelName = state.activeModelName,
                onDraftChange = { onIntent(ChatIntent.DraftChanged(it)) },
                onSend = { onIntent(ChatIntent.SendClicked) },
                onStop = { onIntent(ChatIntent.StopClicked) },
                onAddClick = { showAddSheet = true },
                onModelClick = { showModelSheet = true },
                voiceInput = state.voiceInput,
                voicePreview = state.voicePreview,
                onVoiceClick = { onIntent(ChatIntent.VoiceClicked) },
            )
        }
    }

    if (showAddSheet) {
        AddOptionsModalSheet(
            thinkingSupported = state.thinkingSupported,
            thinkingEnabled = state.thinkingEnabled,
            onThinkingToggle = { onIntent(ChatIntent.ThinkingToggled) },
            onDismiss = { showAddSheet = false },
        )
    }

    if (showModelSheet) {
        ModelSelectorModalSheet(
            availableModels = state.availableModels,
            onSelectModel = { onIntent(ChatIntent.ModelSelected(it)) },
            onManageModels = onOpenModels,
            onDismiss = { showModelSheet = false },
        )
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
