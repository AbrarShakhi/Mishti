package com.abrarshakhi.mishti.core.storage.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MigrationsTest {
    @Test
    fun upgradesEveryNativeVersionWithoutLosingMessagesOrModels() {
        for (version in 1..4) {
            BundledSQLiteDriver().open(":memory:").use { connection ->
                connection.createVersionOne()
                ALL_MIGRATIONS.filter { it.endVersion <= version }.forEach { it.migrate(connection) }
                if (version >=
                    3
                ) {
                    connection.execSQL(
                        "INSERT INTO installed_models (id,name,origin,sizeBytes,installedAtMillis) VALUES ('model','Model','Imported',100,42)",
                    )
                }
                ALL_MIGRATIONS.filter { it.startVersion >= version }.forEach { it.migrate(connection) }
                assertEquals("hello", connection.text("SELECT content FROM chat_messages WHERE id='message'"))
                assertEquals("Session", connection.text("SELECT title FROM chat_sessions WHERE id='session'"))
                assertTrue(connection.columns().containsAll(listOf("tokensPerSecond", "reasoning", "reasoningMillis")))
                if (version >=
                    3
                ) {
                    assertEquals("Model", connection.text("SELECT name FROM installed_models WHERE id='model'"))
                }
                // The migrated model table accepts all fields expected by the shared entity.
                connection.execSQL(
                    "INSERT INTO installed_models VALUES ('new','New','Imported',NULL,NULL,100,NULL,NULL,NULL,NULL,NULL,42)",
                )
            }
        }
    }

    @Test
    fun upgradesEarlyCmpVersionOneWithExistingReasoningColumns() {
        BundledSQLiteDriver().open(":memory:").use { connection ->
            connection.createVersionOne()
            connection.execSQL("ALTER TABLE chat_messages ADD COLUMN tokensPerSecond REAL")
            connection.execSQL("ALTER TABLE chat_messages ADD COLUMN reasoning TEXT")
            connection.execSQL("ALTER TABLE chat_messages ADD COLUMN reasoningMillis INTEGER")
            connection.execSQL("UPDATE chat_messages SET reasoning='thinking', reasoningMillis=20, tokensPerSecond=7.5")
            ALL_MIGRATIONS.forEach { it.migrate(connection) }
            assertEquals("thinking", connection.text("SELECT reasoning FROM chat_messages"))
            assertEquals(1, connection.columns().count { it == "reasoning" })
        }
    }

    private fun SQLiteConnection.createVersionOne() {
        execSQL(
            "CREATE TABLE chat_sessions (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, createdAtMillis INTEGER NOT NULL, updatedAtMillis INTEGER NOT NULL)",
        )
        execSQL(
            "CREATE TABLE chat_messages (id TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, author TEXT NOT NULL, content TEXT NOT NULL, createdAtMillis INTEGER NOT NULL, FOREIGN KEY(sessionId) REFERENCES chat_sessions(id) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_chat_messages_sessionId ON chat_messages(sessionId)")
        execSQL("INSERT INTO chat_sessions VALUES ('session','Session',1,2)")
        execSQL("INSERT INTO chat_messages VALUES ('message','session','User','hello',1)")
    }
    private fun SQLiteConnection.text(sql: String): String = prepare(sql).use {
        assertTrue(it.step())
        it.getText(0)
    }
    private fun SQLiteConnection.columns(): List<String> = prepare("PRAGMA table_info(chat_messages)").use {
        buildList { while (it.step()) add(it.getText(1)) }
    }
}
