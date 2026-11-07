package com.abrarshakhi.mishti.core.storage.preferences.data

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesFileTest {
    @Test
    fun `an Android upgrade continues using the native preferences file`() {
        val directory = Files.createTempDirectory("mishti-preferences").toFile()
        try {
            val native = directory.resolve("datastore/$DATASTORE_FILE_NAME")
            requireNotNull(native.parentFile).mkdirs()
            native.writeText("native preferences")
            assertEquals(native, resolvePreferencesFile(directory))
            assertEquals("native preferences", native.readText())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun `existing CMP preferences take precedence without overwriting either file`() {
        val directory = Files.createTempDirectory("mishti-preferences").toFile()
        try {
            val cmp = directory.resolve(DATASTORE_FILE_NAME)
            cmp.writeText("CMP preferences")
            val native = directory.resolve("datastore/$DATASTORE_FILE_NAME")
            requireNotNull(native.parentFile).mkdirs()
            native.writeText("native preferences")
            assertEquals(cmp, resolvePreferencesFile(directory))
            assertEquals("CMP preferences", cmp.readText())
            assertEquals("native preferences", native.readText())
        } finally {
            directory.deleteRecursively()
        }
    }
}
