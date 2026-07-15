package com.abrarshakhi.mishti.domain.model

data class ChatMessage(
    val role: Role,
    val text: String
) {
    enum class Role { USER, LLM }
}