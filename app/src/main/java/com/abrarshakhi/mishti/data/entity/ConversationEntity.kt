package com.abrarshakhi.mishti.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abrarshakhi.mishti.domain.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val modelId: String,
) {
    fun toDomain() = Conversation(
        id = id,
        title = title,
        createdAt = createdAt,
        modelId = modelId,
    )
}
