package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class SessionGroupsTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 3)
    private val now = today.atTime(12, 0).toInstant(zone).toEpochMilli()

    private fun session(
        id: String,
        daysAgo: Long,
        hour: Int = 9,
    ) = ChatSession(
        id = id,
        title = id,
        createdAtMillis = 0L,
        updatedAtMillis =
            today
                .minusDays(daysAgo)
                .atTime(hour, 0)
                .toInstant(zone)
                .toEpochMilli(),
    )

    @Test
    fun `sessions are bucketed by the calendar day they were last used`() {
        val groups =
            groupSessionsByRecency(
                sessions =
                    listOf(
                        session("today", daysAgo = 0),
                        session("yesterday", daysAgo = 1),
                        session("this week", daysAgo = 3),
                        session("this month", daysAgo = 12),
                        session("older", daysAgo = 45),
                    ),
                now = now,
                zone = zone,
            )

        assertEquals(
            listOf(
                Recency.Today,
                Recency.Yesterday,
                Recency.PreviousWeek,
                Recency.PreviousMonth,
                Recency.Older,
            ),
            groups.map { it.recency },
        )
        assertEquals(listOf("this week"), groups[2].sessions.map { it.id })
    }

    @Test
    fun `yesterday means the previous calendar day, not the last 24 hours`() {
        val groups =
            groupSessionsByRecency(listOf(session("late", daysAgo = 1, hour = 23)), now, zone)

        assertEquals(Recency.Yesterday, groups.single().recency)
    }

    @Test
    fun `groups keep the order of the sessions they are given`() {
        val groups =
            groupSessionsByRecency(
                sessions = listOf(session("newer", 0, hour = 11), session("older", 0, hour = 8)),
                now = now,
                zone = zone,
            )

        assertEquals(listOf("newer", "older"), groups.single().sessions.map { it.id })
    }

    @Test
    fun `no sessions means no groups`() {
        assertTrue(groupSessionsByRecency(emptyList(), now, zone).isEmpty())
    }
}
