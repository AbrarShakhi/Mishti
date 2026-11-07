package com.abrarshakhi.mishti.core.storage.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.mishti.chat.data.local.ChatMessageEntity
import com.abrarshakhi.mishti.chat.data.local.ChatSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseDeviceTest {
    @Test
    fun `updating a session preserves its messages and deleting it cascades`() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = createMishtiDatabase(Room.inMemoryDatabaseBuilder<MishtiDatabase>(context))
        try {
            val dao = database.chatDao()
            val session = ChatSessionEntity("session", "Original", 1, 1)
            dao.upsertSession(session)
            dao.insertMessage(ChatMessageEntity("message", "session", "User", "Hello", 1))
            dao.upsertSession(session.copy(title = "Renamed", updatedAtMillis = 2))
            assertEquals("Hello", dao.observeMessages("session").first().single().content)
            assertEquals("Renamed", dao.observeSessions().first().single().title)
            dao.deleteSession("session")
            assertTrue(dao.observeMessages("session").first().isEmpty())
        } finally {
            database.close()
        }
    }
}
