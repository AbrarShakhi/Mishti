package com.abrarshakhi.mishti.common.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE chat_messages ADD COLUMN tokensPerSecond REAL")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
