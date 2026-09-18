package com.abrarshakhi.mishti.features.chat.fake

import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ChatRepository].
 *
 * Exists because the ViewModel depends on the interface, not on Room — so its behaviour can
 * be tested without a database, a device, or Robolectric.
 */
class FakeChatRepository(
    private val existingSessionId: String? = null,
) : ChatRepository {

    private val sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    private val messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    var createdSessionCount = 0
        private set

    /** When set, every write fails — used to exercise the error path. */
    var failOnAppend: Boolean = false

    override fun observeSessions(): Flow<List<ChatSession>> = sessions

    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> =
        messages.map { it[sessionId].orEmpty() }

    // Mirrors Room's ordering: the most recently touched session, falling back to the id
    // the test seeded.
    override suspend fun latestSessionId(): String? =
        sessions.value.maxByOrNull { it.updatedAtMillis }?.id ?: existingSessionId

    override suspend fun createSession(title: String): String {
        createdSessionCount++
        val id = "created-session-$createdSessionCount"
        sessions.value = sessions.value + ChatSession(id, title, 0L, createdSessionCount.toLong())
        return id
    }

    override suspend fun isSessionEmpty(sessionId: String): Boolean =
        messages.value[sessionId].isNullOrEmpty()

    override suspend fun appendMessage(sessionId: String, message: ChatMessage) {
        if (failOnAppend) error("write failed")
        messages.value = messages.value.toMutableMap().apply {
            this[sessionId] = this[sessionId].orEmpty() + message
        }
    }

    override suspend fun renameSession(sessionId: String, title: String) {
        sessions.value = sessions.value.map {
            if (it.id == sessionId) it.copy(title = title) else it
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        messages.value = messages.value - sessionId
        sessions.value = sessions.value.filterNot { it.id == sessionId }
    }
}
