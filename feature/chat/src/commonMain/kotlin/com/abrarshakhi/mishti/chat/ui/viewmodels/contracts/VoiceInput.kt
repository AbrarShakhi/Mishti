package com.abrarshakhi.mishti.chat.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatMessage
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.core.ui.utils.UiText
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

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
