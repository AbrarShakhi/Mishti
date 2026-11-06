package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.core.llm.domain.EngineState
import com.abrarshakhi.mishti.core.ui.mvi.UiEffect
import com.abrarshakhi.mishti.core.ui.mvi.UiIntent
import com.abrarshakhi.mishti.core.ui.mvi.UiState
import com.abrarshakhi.mishti.core.ui.utils.UiText

data class ChatUiState(
    val sessionId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val streamingResponse: String = "",
    val streamingReasoning: String? = null,
    val isReasoning: Boolean = false,
    val reasoningMillis: Long? = null,
    val isGenerating: Boolean = false,
    val canSend: Boolean = false,
    val canStop: Boolean = false,
    val isLoading: Boolean = true,
    val engineState: EngineState = EngineState.Idle,
    val thinkingSupported: Boolean = false,
    val thinkingEnabled: Boolean = false,
) : UiState

sealed interface ChatIntent : UiIntent {
    data class DraftChanged(val text: String) : ChatIntent

    data object SendClicked : ChatIntent

    data object StopClicked : ChatIntent

    data object ThinkingToggled : ChatIntent
}

sealed interface ChatEffect : UiEffect {
    data class ShowError(val text: UiText) : ChatEffect
}
