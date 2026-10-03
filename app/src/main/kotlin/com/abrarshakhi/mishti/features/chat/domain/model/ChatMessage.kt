package com.abrarshakhi.mishti.features.chat.domain.model


enum class MessageAuthor {
    User,
    Assistant,
}

data class ChatMessage(
    val id: String,
    val author: MessageAuthor,
    val content: String,
    val createdAtMillis: Long,
    val tokensPerSecond: Double? = null,
)
