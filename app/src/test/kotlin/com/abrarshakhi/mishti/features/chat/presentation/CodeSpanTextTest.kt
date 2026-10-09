package com.abrarshakhi.mishti.features.chat.presentation

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CodeSpanTextTest {
    private val parser =
        MarkdownParser(
            GFMFlavourDescriptor(),
            cancellationToken = CancellationToken.NonCancellable,
        )

    private fun codeIn(markdown: CharSequence): String {
        val span = checkNotNull(parser.buildMarkdownTreeFromString(markdown).findCodeSpan())
        return span.codeSpanText(markdown)
    }

    private fun ASTNode.findCodeSpan(): ASTNode? =
        if (type == MarkdownElementTypes.CODE_SPAN) {
            this
        } else {
            children.firstNotNullOfOrNull { it.findCodeSpan() }
        }

    @Test
    fun `the backticks are left out`() {
        assertEquals("print", codeIn("Call `print` here."))
    }

    @Test
    fun `a longer run of backticks lets the code hold a backtick`() {
        assertEquals("a ` b", codeIn("Use ``a ` b`` there."))
    }

    @Test
    fun `one space just inside each pair of backticks is dropped`() {
        assertEquals("`tick`", codeIn("Write `` `tick` `` like so."))
    }

    @Test
    fun `code of only spaces keeps them`() {
        assertEquals("  ", codeIn("Two spaces: `  `."))
    }

    @Test
    fun `a line break inside the span becomes a space`() {
        assertEquals("foo bar", codeIn("Run `foo\nbar` now."))
    }
}
