package com.abrarshakhi.mishti.core.storage.chat.mapper

import com.abrarshakhi.mishti.core.domain.chat.model.ChatMessage
import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.domain.chat.model.MessageAuthor
import com.abrarshakhi.mishti.core.storage.chat.local.ChatMessageEntity
import com.abrarshakhi.mishti.core.storage.chat.local.ChatSessionEntity

fun ChatSessionEntity.toDomain() = ChatSession(
    id = id,
    title = title,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

@Suppress("UNUSED")
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
    reasoning = reasoning,
    reasoningMillis = reasoningMillis,
)

fun ChatMessage.toEntity(sessionId: String) = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    author = author.name,
    content = content,
    createdAtMillis = createdAtMillis,
    tokensPerSecond = tokensPerSecond,
    reasoning = reasoning,
    reasoningMillis = reasoningMillis,
)
