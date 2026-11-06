package com.abrarshakhi.mishti.chat.domain

import com.abrarshakhi.mishti.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.chat.domain.repository.ChatRepository
import com.abrarshakhi.mishti.chat.domain.usecase.CreateNewSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateNewSessionUseCaseTest {

    private class FakeChatRepository : ChatRepository {
        val sessions = mutableListOf<ChatSession>()
        val emptyMap = mutableMapOf<String, Boolean>()
        var createCount = 0

        override fun observeSessions(): Flow<List<ChatSession>> = flowOf(sessions)
        override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> = flowOf(emptyList())
        override suspend fun latestSessionId(): String? = sessions.lastOrNull()?.id
        override suspend fun createSession(title: String): String {
            createCount++
            val id = "session-$createCount"
            sessions.add(ChatSession(id, title, 0L, 0L))
            emptyMap[id] = true
            return id
        }
        override suspend fun isSessionEmpty(sessionId: String): Boolean = emptyMap[sessionId] ?: true
        override suspend fun appendMessage(sessionId: String, message: ChatMessage) {
            emptyMap[sessionId] = false
        }
        override suspend fun renameSession(sessionId: String, title: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }

    @Test
    fun creates_new_session_when_none_exist() = runBlocking {
        val repo = FakeChatRepository()
        val useCase = CreateNewSessionUseCase(repo)

        val id = useCase()
        assertEquals("session-1", id)
        assertEquals(1, repo.createCount)
    }

    @Test
    fun reuses_latest_session_if_empty() = runBlocking {
        val repo = FakeChatRepository()
        val useCase = CreateNewSessionUseCase(repo)

        val id1 = useCase()
        val id2 = useCase()

        assertEquals("session-1", id1)
        assertEquals("session-1", id2)
        assertEquals(1, repo.createCount)
    }

    @Test
    fun creates_new_session_if_latest_is_not_empty() = runBlocking {
        val repo = FakeChatRepository()
        val useCase = CreateNewSessionUseCase(repo)

        val id1 = useCase()
        repo.appendMessage(id1, ChatMessage("m1", MessageAuthor.User, "Hello", 0L))

        val id2 = useCase()
        assertEquals("session-1", id1)
        assertEquals("session-2", id2)
        assertEquals(2, repo.createCount)
    }
}
