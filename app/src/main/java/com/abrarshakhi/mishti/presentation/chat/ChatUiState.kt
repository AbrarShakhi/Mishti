package com.abrarshakhi.mishti.presentation.chat

import com.abrarshakhi.mishti.domain.model.ChatMessage

data class ChatUiState(
    val conversationGroups: List<ConversationGroup> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val activeModelId: String = "tinyllama-1.1b-q4",
    val activeModelName: String = "TinyLlama",
    val isGenerating: Boolean = false,
    val isModelLoading: Boolean = false,
    val modelLoadError: String? = null,
)
