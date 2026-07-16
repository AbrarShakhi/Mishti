package com.abrarshakhi.mishti.presentation.chat

sealed interface ChatIntent {
    data object NewChat : ChatIntent
    data class SelectConversation(val conversationId: String) : ChatIntent
    data class InputChanged(val text: String) : ChatIntent
    data object SendMessage : ChatIntent
}
