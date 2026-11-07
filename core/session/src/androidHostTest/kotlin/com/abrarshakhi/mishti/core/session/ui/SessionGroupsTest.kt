package com.abrarshakhi.mishti.core.session.ui

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionGroupsTest {
    private lateinit var originalZone: TimeZone

    @BeforeTest
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @AfterTest
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 3)
    private val now = today.atTime(12, 0).toInstant(zone).toEpochMilli()

    private fun session(id: String, daysAgo: Long, hour: Int = 9) = ChatSession(
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
            groupSessionsByRecency(listOf(session("late", daysAgo = 1, hour = 23)), now)

        assertEquals(Recency.Yesterday, groups.single().recency)
    }

    @Test
    fun `groups keep the order of the sessions they are given`() {
        val groups =
            groupSessionsByRecency(
                sessions = listOf(session("newer", 0, hour = 11), session("older", 0, hour = 8)),
                now = now,
            )

        assertEquals(listOf("newer", "older"), groups.single().sessions.map { it.id })
    }

    @Test
    fun `no sessions means no groups`() {
        assertTrue(groupSessionsByRecency(emptyList(), now).isEmpty())
    }

    @Test
    fun `a change of year still uses the previous calendar day`() {
        val now = ZonedDateTime.parse("2027-01-01T00:05:00Z").toInstant().toEpochMilli()
        val previous = ZonedDateTime.parse("2026-12-31T23:55:00Z").toInstant().toEpochMilli()
        val groups = groupSessionsByRecency(listOf(ChatSession("old", "Old", previous, previous)), now)
        assertEquals(Recency.Yesterday, groups.single().recency)
    }

    @Test
    fun `daylight saving changes do not turn yesterday into today`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        val now = ZonedDateTime.parse("2026-03-09T00:15:00-04:00[America/New_York]").toInstant().toEpochMilli()
        val previous = ZonedDateTime.parse("2026-03-08T00:30:00-05:00[America/New_York]").toInstant().toEpochMilli()
        val groups = groupSessionsByRecency(listOf(ChatSession("old", "Old", previous, previous)), now)
        assertEquals(Recency.Yesterday, groups.single().recency)
    }
}
