package com.abrarshakhi.mishti.core.storage.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// The early CMP build also used version 1, with the later chat columns already present.
// Checking columns lets both that build and the original Android database upgrade safely.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.addMessageColumnIfMissing("tokensPerSecond", "REAL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(CREATE_INSTALLED_MODELS)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.addMessageColumnIfMissing("reasoning", "TEXT")
        connection.addMessageColumnIfMissing("reasoningMillis", "INTEGER")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        // Version 5 unifies the native Android and CMP schema histories.
        connection.execSQL(CREATE_INSTALLED_MODELS)
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

private fun SQLiteConnection.addMessageColumnIfMissing(name: String, type: String) {
    val exists = prepare("PRAGMA table_info(`chat_messages`)").use { statement ->
        var found = false
        while (statement.step()) {
            if (statement.getText(1) == name) found = true
        }
        found
    }
    if (!exists) execSQL("ALTER TABLE `chat_messages` ADD COLUMN `$name` $type")
}

private const val CREATE_INSTALLED_MODELS =
    "CREATE TABLE IF NOT EXISTS `installed_models` (" +
        "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `origin` TEXT NOT NULL, " +
        "`quantization` TEXT, `parametersLabel` TEXT, `sizeBytes` INTEGER NOT NULL, " +
        "`hfRepo` TEXT, `hfFile` TEXT, `architecture` TEXT, `contextLength` INTEGER, " +
        "`license` TEXT, `installedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))"
