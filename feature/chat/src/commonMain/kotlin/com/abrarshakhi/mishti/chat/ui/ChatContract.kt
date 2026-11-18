package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.core.domain.chat.model.ChatMessage
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.core.ui.mvi.UiEffect
import com.abrarshakhi.mishti.core.ui.mvi.UiIntent
import com.abrarshakhi.mishti.core.ui.mvi.UiState
import com.abrarshakhi.mishti.core.ui.utils.UiText

data class ChatUiState(
    val sessionId: String? = null,
    val title: String = "",
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
    val availableModels: List<SelectableModel> = emptyList(),
    val activeModelName: String? = null,
    val voiceInput: VoiceInput = VoiceInput.Unavailable,
    val voicePreview: String = "",
) : UiState

enum class VoiceInput {
    Unavailable,
    Idle,
    RequestingPermission,
    Preparing,
    Listening,
    Finishing,
    ;

    val isActive: Boolean
        get() = this != Unavailable && this != Idle
}

sealed interface ChatIntent : UiIntent {
    data class DraftChanged(val text: String) : ChatIntent

    data object SendClicked : ChatIntent

    data object StopClicked : ChatIntent

    data object VoiceClicked : ChatIntent

    data class VoicePermissionResult(val granted: Boolean) : ChatIntent

    data object VoiceCancelled : ChatIntent

    data object ThinkingToggled : ChatIntent

    data class ModelSelected(val modelId: String) : ChatIntent
}

sealed interface ChatEffect : UiEffect {
    data class ShowError(val text: UiText) : ChatEffect

    data object RequestMicrophonePermission : ChatEffect
}
