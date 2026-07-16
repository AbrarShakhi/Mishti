package com.abrarshakhi.mishti.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abrarshakhi.mishti.data.entity.ConversationEntity
import com.abrarshakhi.mishti.data.entity.MessageEntity
import com.abrarshakhi.mishti.data.entity.ModelEntity

@Database(
    entities = [
        ModelEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun conversationDao(): ConversationDao
}
