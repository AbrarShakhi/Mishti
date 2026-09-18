package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.mvi.UiEffect
import com.abrarshakhi.mishti.common.mvi.UiIntent
import com.abrarshakhi.mishti.common.mvi.UiState
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage

data class ChatUiState(
    val sessionId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val streamingResponse: String = "",
    val isGenerating: Boolean = false,
    val canSend: Boolean = false,
    val canStop: Boolean = false,
    val isLoading: Boolean = true,
    val engineState: EngineState = EngineState.Idle,
) : UiState

sealed interface ChatIntent : UiIntent {
    data class DraftChanged(val text: String) : ChatIntent

    data object SendClicked : ChatIntent

    data object StopClicked : ChatIntent
}

sealed interface ChatEffect : UiEffect {
    data class ShowError(val text: String) : ChatEffect
}
