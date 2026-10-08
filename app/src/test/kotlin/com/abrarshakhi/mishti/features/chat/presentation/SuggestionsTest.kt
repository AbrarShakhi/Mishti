package com.abrarshakhi.mishti.features.chat.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionsTest {

    private val pool = (1..16).map { "Idea $it" }

    @Test
    fun `four different suggestions are picked from the pool`() {
        val picked = pickSuggestions(pool, count = 4, seed = 7)

        assertEquals(4, picked.size)
        assertEquals(4, picked.toSet().size)
        assertTrue(pool.containsAll(picked))
    }

    @Test
    fun `the same seed gives the same suggestions, so they survive rotation`() {
        assertEquals(pickSuggestions(pool, 4, seed = 42), pickSuggestions(pool, 4, seed = 42))
    }

    @Test
    fun `a new seed usually gives a different set`() {
        assertNotEquals(pickSuggestions(pool, 4, seed = 1), pickSuggestions(pool, 4, seed = 2))
    }

    @Test
    fun `a small pool is shown in full without repeats`() {
        assertEquals(setOf("A", "B"), pickSuggestions(listOf("A", "B", "A"), 4, seed = 3).toSet())
    }
}
