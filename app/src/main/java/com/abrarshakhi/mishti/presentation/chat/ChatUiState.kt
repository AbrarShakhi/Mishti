package com.abrarshakhi.mishti.presentation.chat

import com.abrarshakhi.mishti.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val isModelLoaded: Boolean = false,
    val loadingError: String? = null,
    val conversationGroups: List<ConversationGroup> = emptyList(),
    val activeConversationId: String? = null
)