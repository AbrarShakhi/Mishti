package com.abrarshakhi.mishti.common.llm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReasoningTest {

    @Test
    fun `text without tags is all answer`() {
        assertEquals(ReasoningSplit(null, "Hello there", false), splitReasoning("Hello there"))
    }

    @Test
    fun `a think block is split from the answer`() {
        assertEquals(
            ReasoningSplit("Plan the reply.", "Hello!", false),
            splitReasoning("<think>\nPlan the reply.\n</think>\n\nHello!"),
        )
    }

    @Test
    fun `an unfinished think block is still reasoning`() {
        assertEquals(ReasoningSplit("Still going", "", true), splitReasoning("\n<think>Still going"))
    }

    @Test
    fun `a partial opening tag shows nothing yet`() {
        assertEquals(ReasoningSplit(null, "", false), splitReasoning("<thi"))
    }

    @Test
    fun `a partial closing tag is held back while streaming`() {
        assertEquals(ReasoningSplit("Almost", "", true), splitReasoning("<think>Almost</thi"))
        assertEquals(ReasoningSplit(null, "a", false), splitReasoning("a<"))
    }

    @Test
    fun `a closing tag without an opening one marks what came before as reasoning`() {
        assertEquals(
            ReasoningSplit("The template opened it.", "Answer", false),
            splitReasoning("The template opened it.\n</think>\n\nAnswer"),
        )
    }

    @Test
    fun `an empty think block leaves no reasoning`() {
        assertEquals(ReasoningSplit(null, "Answer", false), splitReasoning("<think>\n\n</think>\n\nAnswer"))
    }

    @Test
    fun `a finished reply keeps everything it ends with`() {
        assertEquals(ReasoningSplit(null, "1 <", false), splitReasoning("1 <", complete = true))
        assertEquals(ReasoningSplit(null, "<th", false), splitReasoning("<th", complete = true))
    }

    @Test
    fun `a reply cut off while thinking is no longer reasoning once complete`() {
        assertEquals(ReasoningSplit("Cut </th", "", false), splitReasoning("<think>Cut </th", complete = true))
    }

    @Test
    fun `templates that mention think tags support thinking`() {
        assertTrue(templateSupportsThinking("{{ '<think>\\n\\n</think>\\n\\n' }}"))
        assertFalse(templateSupportsThinking("{{ '<|im_start|>assistant\\n' }}"))
    }
}
