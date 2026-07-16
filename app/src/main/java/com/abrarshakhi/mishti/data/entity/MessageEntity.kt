package com.abrarshakhi.mishti.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abrarshakhi.mishti.domain.model.ChatMessage

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            // Deleting a conversation cascades to its messages — no orphans.
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("conversationId")],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String,           // "USER" or "LLM"
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toDomain() = ChatMessage(
        role = ChatMessage.Role.valueOf(role),
        text = text,
    )
}

fun ChatMessage.toEntity(conversationId: String) = MessageEntity(
    conversationId = conversationId,
    role = role.name,
    text = text,
)
