package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.common.mvi.UiEffect
import com.abrarshakhi.mishti.common.mvi.UiIntent
import com.abrarshakhi.mishti.common.mvi.UiState
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession

data class SessionsUiState(
    val sessions: List<ChatSession> = emptyList(),
    val actionsFor: ChatSession? = null,
    val renaming: RenameState? = null,
    val deleting: ChatSession? = null,
) : UiState

data class RenameState(
    val sessionId: String,
    val title: String,
) {
    val canConfirm: Boolean get() = title.isNotBlank()
}

sealed interface SessionsIntent : UiIntent {
    data object NewChatClicked : SessionsIntent

    data class SessionSelected(val sessionId: String) : SessionsIntent

    data class SessionLongPressed(val sessionId: String) : SessionsIntent

    data object ActionsDismissed : SessionsIntent

    data object RenameRequested : SessionsIntent

    data class RenameTitleChanged(val title: String) : SessionsIntent

    data object RenameConfirmed : SessionsIntent

    data object RenameCancelled : SessionsIntent

    data object DeleteRequested : SessionsIntent

    data class DeleteConfirmed(val visibleSessionId: String?) : SessionsIntent

    data object DeleteCancelled : SessionsIntent
}

sealed interface SessionsEffect : UiEffect {
    data class OpenSession(val sessionId: String) : SessionsEffect
}
