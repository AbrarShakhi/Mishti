package com.abrarshakhi.mishti.common.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE chat_messages ADD COLUMN tokensPerSecond REAL")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `installed_models` (" +
                    "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `origin` TEXT NOT NULL, " +
                    "`quantization` TEXT, `parametersLabel` TEXT, `sizeBytes` INTEGER NOT NULL, " +
                    "`hfRepo` TEXT, `hfFile` TEXT, `architecture` TEXT, `contextLength` INTEGER, " +
                    "`license` TEXT, `installedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            )
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE chat_messages ADD COLUMN reasoning TEXT")
            db.execSQL("ALTER TABLE chat_messages ADD COLUMN reasoningMillis INTEGER")
        }
    }

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
