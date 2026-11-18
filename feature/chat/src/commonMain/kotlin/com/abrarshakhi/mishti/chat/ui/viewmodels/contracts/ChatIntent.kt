package com.abrarshakhi.mishti.chat.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatMessage
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.core.ui.utils.UiText
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

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
