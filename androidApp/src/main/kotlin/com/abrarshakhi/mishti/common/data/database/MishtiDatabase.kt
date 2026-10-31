package com.abrarshakhi.mishti.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abrarshakhi.mishti.features.chat.data.local.ChatDao
import com.abrarshakhi.mishti.features.chat.data.local.ChatMessageEntity
import com.abrarshakhi.mishti.features.chat.data.local.ChatSessionEntity
import com.abrarshakhi.mishti.features.models.data.local.InstalledModelDao
import com.abrarshakhi.mishti.features.models.data.local.InstalledModelEntity

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class, InstalledModelEntity::class],
    version = DATABASE_VERSION,
    exportSchema = true,
)
abstract class MishtiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    abstract fun installedModelDao(): InstalledModelDao
}

const val DATABASE_VERSION = 4

const val DATABASE_NAME = "mishti.db"
