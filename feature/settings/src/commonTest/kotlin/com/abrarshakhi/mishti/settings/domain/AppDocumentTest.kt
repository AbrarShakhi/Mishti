package com.abrarshakhi.mishti.settings.domain

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.settings.domain.model.DocumentLink
import com.abrarshakhi.mishti.settings.domain.model.resolveDocumentLink
import com.abrarshakhi.mishti.settings.domain.usecase.stripLeadingTitle
import com.abrarshakhi.mishti.settings.domain.usecase.unindentWrappedLines
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun `links in the root contributing guide resolve from the repository root`() {
        assertEquals(
            DocumentLink.External("https://github.com/AbrarShakhi/Mishti/blob/main/README.md"),
            resolveDocumentLink("README.md", AppDocument.Contributing),
        )
    }

    @Test
    fun `anchors remain in the current document and protocol relative links open on the web`() {
        assertEquals(DocumentLink.Internal(AppDocument.Terms), resolveDocumentLink("#contact", AppDocument.Terms))
        assertEquals(DocumentLink.External("https://example.org/page"), resolveDocumentLink("//example.org/page"))
    }
}
