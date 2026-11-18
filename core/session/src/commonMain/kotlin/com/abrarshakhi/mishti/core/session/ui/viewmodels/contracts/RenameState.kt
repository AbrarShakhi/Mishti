package com.abrarshakhi.mishti.core.session.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState

data class RenameState(val sessionId: String, val title: String) {
    val canConfirm: Boolean get() = title.isNotBlank()
}
