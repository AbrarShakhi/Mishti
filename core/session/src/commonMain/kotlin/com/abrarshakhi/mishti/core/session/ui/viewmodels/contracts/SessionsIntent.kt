package com.abrarshakhi.mishti.core.session.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

sealed interface SessionsIntent : UiIntent {
    data object DrawerClosed : SessionsIntent

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
