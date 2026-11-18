package com.abrarshakhi.mishti.core.session.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

data class SessionsUiState(
    val sessions: List<ChatSession> = emptyList(),
    val actionsFor: ChatSession? = null,
    val renaming: RenameState? = null,
    val deleting: ChatSession? = null,
) : UiState
