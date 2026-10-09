package com.abrarshakhi.mishti.features.settings.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDocumentTest {
    @Test
    fun `links between bundled documents stay in the app`() {
        assertEquals(
            DocumentLink.Internal(AppDocument.Privacy),
            resolveDocumentLink("privacy-policy.md"),
        )
        assertEquals(
            DocumentLink.Internal(AppDocument.Credits),
            resolveDocumentLink("credits.md#typefaces"),
        )
        assertEquals(
            DocumentLink.Internal(AppDocument.Contributing),
            resolveDocumentLink("../CONTRIBUTING.md"),
        )
    }

    @Test
    fun `links to other repository files open on GitHub`() {
        assertEquals(
            DocumentLink.External("https://github.com/AbrarShakhi/Mishti/blob/main/LICENSE"),
            resolveDocumentLink("../LICENSE"),
        )
        assertEquals(
            DocumentLink.External(
                "https://github.com/AbrarShakhi/Mishti/blob/main/hub/catalog.v1.json",
            ),
            resolveDocumentLink("../hub/catalog.v1.json"),
        )
    }

    @Test
    fun `web and mail links are opened as they are`() {
        assertEquals(
            DocumentLink.External("https://huggingface.co/privacy"),
            resolveDocumentLink("https://huggingface.co/privacy"),
        )
        assertEquals(
            DocumentLink.External("mailto:someone@example.com"),
            resolveDocumentLink("mailto:someone@example.com"),
        )
    }

    @Test
    fun `wrapped list lines lose their indent so no double spaces appear`() {
        val source =
            "- **Private chat.** Stored on your phone,\n  with a drawer.\n  - Nested item\n    wraps here."

        assertEquals(
            "- **Private chat.** Stored on your phone,\nwith a drawer.\n  - Nested item\nwraps here.",
            unindentWrappedLines(source),
        )
    }

    @Test
    fun `code blocks keep their indentation`() {
        val source = "1. Run:\n   ```bash\n   ./gradlew \\\n       assembleDebug\n   ```"

        assertEquals(source, unindentWrappedLines(source))
    }

    @Test
    fun `the document's own heading is dropped because the app bar shows it`() {
        assertEquals("Body text.", stripLeadingTitle("# Credits\n\nBody text."))
        assertEquals("## Section\nText", stripLeadingTitle("## Section\nText"))
    }
}
