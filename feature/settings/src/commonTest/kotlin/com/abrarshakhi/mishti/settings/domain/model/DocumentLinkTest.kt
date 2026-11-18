package com.abrarshakhi.mishti.settings.domain.model

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentLinkTest {
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
