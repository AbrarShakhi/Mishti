package com.abrarshakhi.mishti.core.storage.chat.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryChatDao : ChatDao {
    private val sessionsState = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    private val messagesState = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override fun observeSessions(): Flow<List<ChatSessionEntity>> =
        sessionsState.map { it.sortedByDescending { s -> s.updatedAtMillis } }

    override fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>> = messagesState.map { msgs ->
        msgs.filter { it.sessionId == sessionId }.sortedBy { it.createdAtMillis }
    }

    override suspend fun latestSessionId(): String? = sessionsState.value.maxByOrNull { it.updatedAtMillis }?.id

    override suspend fun titleOf(sessionId: String): String? =
        sessionsState.value.firstOrNull { it.id == sessionId }?.title

    override suspend fun messageCount(sessionId: String): Int = messagesState.value.count { it.sessionId == sessionId }

    override suspend fun upsertSession(session: ChatSessionEntity) {
        sessionsState.update { current ->
            current.filterNot { it.id == session.id } + session
        }
    }

    override suspend fun insertMessage(message: ChatMessageEntity) {
        messagesState.update { current ->
            current.filterNot { it.id == message.id } + message
        }
    }

    override suspend fun touchSession(sessionId: String, updatedAt: Long) {
        sessionsState.update { current ->
            current.map { s ->
                if (s.id == sessionId) s.copy(updatedAtMillis = updatedAt) else s
            }
        }
    }

    override suspend fun renameSession(sessionId: String, title: String) {
        sessionsState.update { current ->
            current.map { s ->
                if (s.id == sessionId) s.copy(title = title) else s
            }
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionsState.update { current -> current.filterNot { it.id == sessionId } }
        messagesState.update { current -> current.filterNot { it.sessionId == sessionId } }
    }
}
