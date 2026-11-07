package com.abrarshakhi.mishti.core.storage.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.abrarshakhi.mishti.chat.data.local.ChatDao
import com.abrarshakhi.mishti.chat.data.local.ChatMessageEntity
import com.abrarshakhi.mishti.chat.data.local.ChatSessionEntity
import com.abrarshakhi.mishti.models.data.local.InstalledModelDao
import com.abrarshakhi.mishti.models.data.local.InstalledModelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

const val DATABASE_VERSION = 5
const val DATABASE_NAME = "mishti.db"

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        InstalledModelEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = true,
)
@ConstructedBy(MishtiDatabaseConstructor::class)
abstract class MishtiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun installedModelDao(): InstalledModelDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MishtiDatabaseConstructor : RoomDatabaseConstructor<MishtiDatabase> {
    override fun initialize(): MishtiDatabase
}

fun createMishtiDatabase(builder: RoomDatabase.Builder<MishtiDatabase>): MishtiDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(*ALL_MIGRATIONS)
    .build()
