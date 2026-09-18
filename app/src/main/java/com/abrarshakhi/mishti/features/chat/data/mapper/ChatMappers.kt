package com.abrarshakhi.mishti.features.chat.data.mapper

import com.abrarshakhi.mishti.features.chat.data.local.ChatMessageEntity
import com.abrarshakhi.mishti.features.chat.data.local.ChatSessionEntity
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor

fun ChatSessionEntity.toDomain() = ChatSession(
    id = id,
    title = title,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun ChatSession.toEntity() = ChatSessionEntity(
    id = id,
    title = title,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    author = runCatching { MessageAuthor.valueOf(author) }.getOrDefault(MessageAuthor.Assistant),
    content = content,
    createdAtMillis = createdAtMillis,
    tokensPerSecond = tokensPerSecond,
)

fun ChatMessage.toEntity(sessionId: String) = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    author = author.name,
    content = content,
    createdAtMillis = createdAtMillis,
    tokensPerSecond = tokensPerSecond,
)
