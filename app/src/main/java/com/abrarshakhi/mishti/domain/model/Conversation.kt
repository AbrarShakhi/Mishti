package com.abrarshakhi.mishti.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val modelId: String,
)