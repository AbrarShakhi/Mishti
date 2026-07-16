package com.abrarshakhi.mishti.data.repository

import com.abrarshakhi.mishti.data.db.ConversationDao
import com.abrarshakhi.mishti.data.entity.ConversationEntity
import com.abrarshakhi.mishti.data.entity.toEntity
import com.abrarshakhi.mishti.domain.model.ChatMessage
import com.abrarshakhi.mishti.domain.model.Conversation
import com.abrarshakhi.mishti.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ConversationRepositoryImpl(
    private val dao: ConversationDao,
) : ConversationRepository {

    override fun observeAll(): Flow<List<Conversation>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun create(modelId: String): String {
        val id = UUID.randomUUID().toString()
        dao.upsert(
            ConversationEntity(
                id = id,
                title = "New chat",
                createdAt = System.currentTimeMillis(),
                modelId = modelId,
            )
        )
        return id
    }

    override suspend fun updateTitle(conversationId: String, title: String) =
        dao.updateTitle(conversationId, title)

    override suspend fun delete(conversationId: String) =
        dao.delete(conversationId)

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> =
        dao.observeMessages(conversationId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addMessage(conversationId: String, message: ChatMessage) =
        dao.insertMessage(message.toEntity(conversationId))
}
