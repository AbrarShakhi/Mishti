package com.abrarshakhi.mishti.core.session.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

sealed interface SessionsEffect : UiEffect {
    data class OpenSession(val sessionId: String) : SessionsEffect
}
