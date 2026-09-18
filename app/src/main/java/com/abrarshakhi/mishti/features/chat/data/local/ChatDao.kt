package com.abrarshakhi.mishti.features.chat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAtMillis DESC")
    fun observeSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAtMillis ASC")
    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT id FROM chat_sessions ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun latestSessionId(): String?

    @Query("SELECT title FROM chat_sessions WHERE id = :sessionId")
    suspend fun titleOf(sessionId: String): String?

    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun messageCount(sessionId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_sessions SET updatedAtMillis = :updatedAt WHERE id = :sessionId")
    suspend fun touchSession(sessionId: String, updatedAt: Long)

    @Query("UPDATE chat_sessions SET title = :title WHERE id = :sessionId")
    suspend fun renameSession(sessionId: String, title: String)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Transaction
    suspend fun insertMessageAndTouchSession(
        message: ChatMessageEntity,
        titleIfUntitled: String?,
    ) {
        insertMessage(message)
        touchSession(message.sessionId, message.createdAtMillis)
        if (titleIfUntitled != null) renameSession(message.sessionId, titleIfUntitled)
    }
}
