package com.abrarshakhi.mishti.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abrarshakhi.mishti.features.chat.data.local.ChatDao
import com.abrarshakhi.mishti.features.chat.data.local.ChatMessageEntity
import com.abrarshakhi.mishti.features.chat.data.local.ChatSessionEntity

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class],
    version = DATABASE_VERSION,
    exportSchema = true,
)
abstract class MishtiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

const val DATABASE_VERSION = 2

const val DATABASE_NAME = "mishti.db"
