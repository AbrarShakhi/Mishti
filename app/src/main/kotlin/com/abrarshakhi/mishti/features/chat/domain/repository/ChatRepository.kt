package com.abrarshakhi.mishti.features.chat.domain.repository

import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun observeSessions(): Flow<List<ChatSession>>

    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>

    suspend fun latestSessionId(): String?

    suspend fun createSession(title: String): String

    suspend fun isSessionEmpty(sessionId: String): Boolean

    suspend fun appendMessage(sessionId: String, message: ChatMessage)

    suspend fun renameSession(sessionId: String, title: String)

    suspend fun deleteSession(sessionId: String)
}
