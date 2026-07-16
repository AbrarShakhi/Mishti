package com.abrarshakhi.mishti.domain.repository

import com.abrarshakhi.mishti.domain.model.ChatMessage
import com.abrarshakhi.mishti.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    fun observeAll(): Flow<List<Conversation>>
    suspend fun create(modelId: String): String
    suspend fun updateTitle(conversationId: String, title: String)
    suspend fun delete(conversationId: String)
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun addMessage(conversationId: String, message: ChatMessage)
}
