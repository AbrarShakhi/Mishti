package com.abrarshakhi.mishti.chat.data

import com.abrarshakhi.mishti.chat.data.local.InMemoryChatDao
import com.abrarshakhi.mishti.chat.data.repository.RoomChatRepository
import com.abrarshakhi.mishti.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.chat.domain.model.UNTITLED_SESSION
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomChatRepositoryTest {

    @Test
    fun session_lifecycle() = runBlocking {
        val dao = InMemoryChatDao()
        val repo = RoomChatRepository(
            dao = dao,
            clock = { 1000L },
            newId = { "session-1" },
        )

        val id = repo.createSession(UNTITLED_SESSION)
        assertEquals("session-1", id)
        assertTrue(repo.isSessionEmpty(id))

        val sessions = repo.observeSessions().first()
        assertEquals(1, sessions.size)
        assertEquals(UNTITLED_SESSION, sessions[0].title)

        repo.appendMessage(id, ChatMessage("msg-1", MessageAuthor.User, "What is Kotlin?", 1000L))
        assertFalse(repo.isSessionEmpty(id))

        val updatedSessions = repo.observeSessions().first()
        assertEquals("What is Kotlin?", updatedSessions[0].title)

        val messages = repo.observeMessages(id).first()
        assertEquals(1, messages.size)
        assertEquals("What is Kotlin?", messages[0].content)

        repo.renameSession(id, "Renamed Session")
        val renamedSessions = repo.observeSessions().first()
        assertEquals("Renamed Session", renamedSessions[0].title)

        repo.deleteSession(id)
        val afterDeleteSessions = repo.observeSessions().first()
        assertTrue(afterDeleteSessions.isEmpty())
        val afterDeleteMessages = repo.observeMessages(id).first()
        assertTrue(afterDeleteMessages.isEmpty())
    }
}
