package com.abrarshakhi.mishti.chat.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatMessage
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.core.ui.utils.UiText
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

sealed interface ChatEffect : UiEffect {
    data class ShowError(val text: UiText) : ChatEffect

    data object RequestMicrophonePermission : ChatEffect
}
