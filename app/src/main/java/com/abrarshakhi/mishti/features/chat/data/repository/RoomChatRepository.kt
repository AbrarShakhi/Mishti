package com.abrarshakhi.mishti.features.chat.data.repository

import com.abrarshakhi.mishti.features.chat.data.local.ChatDao
import com.abrarshakhi.mishti.features.chat.data.local.ChatSessionEntity
import com.abrarshakhi.mishti.features.chat.data.mapper.toDomain
import com.abrarshakhi.mishti.features.chat.data.mapper.toEntity
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.features.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.features.chat.domain.model.sessionTitleFrom
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomChatRepository(
    private val dao: ChatDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ChatRepository {

    override fun observeSessions(): Flow<List<ChatSession>> =
        dao.observeSessions().map { rows -> rows.map { it.toDomain() } }.flowOn(ioDispatcher)

    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> =
        dao.observeMessages(sessionId).map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun latestSessionId(): String? =
        withContext(ioDispatcher) { dao.latestSessionId() }

    override suspend fun createSession(title: String): String = withContext(ioDispatcher) {
        val now = clock()
        val session = ChatSessionEntity(
            id = newId(),
            title = title,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
        dao.upsertSession(session)
        session.id
    }

    override suspend fun isSessionEmpty(sessionId: String): Boolean =
        withContext(ioDispatcher) { dao.messageCount(sessionId) == 0 }

    override suspend fun appendMessage(sessionId: String, message: ChatMessage) {
        withContext(ioDispatcher) {
            val newTitle =
                if (message.author == MessageAuthor.User && dao.titleOf(sessionId) == UNTITLED_SESSION) {
                    sessionTitleFrom(message.content)
                } else {
                    null
                }

            dao.insertMessageAndTouchSession(
                message = message.toEntity(sessionId),
                titleIfUntitled = newTitle,
            )
        }
    }

    override suspend fun renameSession(sessionId: String, title: String) {
        withContext(ioDispatcher) { dao.renameSession(sessionId, title) }
    }

    override suspend fun deleteSession(sessionId: String) {
        withContext(ioDispatcher) { dao.deleteSession(sessionId) }
    }
}
