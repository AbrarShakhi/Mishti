package com.abrarshakhi.mishti.settings.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentFormattingTest {
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
