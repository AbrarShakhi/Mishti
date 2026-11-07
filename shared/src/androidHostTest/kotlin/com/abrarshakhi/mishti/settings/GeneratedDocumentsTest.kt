package com.abrarshakhi.mishti.settings

import com.abrarshakhi.mishti.settings.domain.model.AppDocument
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratedDocumentsTest {
    @Test
    fun `every document is copied from the repository into prepared shared resources`() {
        val prepared = File("build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources")
        for (document in AppDocument.entries) {
            val source = if (document == AppDocument.Contributing) {
                File("../CONTRIBUTING.md")
            } else {
                File("../docs/${document.fileName}")
            }
            val generated = File(prepared, "files/docs/${document.fileName}")
            assertTrue(generated.isFile, document.fileName)
            assertTrue(generated.length() > 0, document.fileName)
            assertContentEquals(source.readBytes(), generated.readBytes(), document.fileName)
        }
        assertFalse(File(prepared, "files/catalog.v1.json").exists())
        assertTrue(File(prepared, "font/inter.ttf").isFile)
        assertTrue(File(prepared, "files/onboarding_chat.json").isFile)
    }
}
