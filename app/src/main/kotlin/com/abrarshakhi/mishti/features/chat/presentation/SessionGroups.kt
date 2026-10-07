package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class Recency(val label: String) {
    Today("Today"),
    Yesterday("Yesterday"),
    PreviousWeek("Previous 7 days"),
    PreviousMonth("Previous 30 days"),
    Older("Older"),
}

data class SessionGroup(val recency: Recency, val sessions: List<ChatSession>)

fun groupSessionsByRecency(
    sessions: List<ChatSession>,
    now: Long,
    zone: ZoneId,
): List<SessionGroup> {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()

    return sessions
        .groupBy { session ->
            val day = Instant.ofEpochMilli(session.updatedAtMillis).atZone(zone).toLocalDate()
            val daysAgo = ChronoUnit.DAYS.between(day, today)
            when {
                daysAgo <= 0 -> Recency.Today
                daysAgo == 1L -> Recency.Yesterday
                daysAgo < 7 -> Recency.PreviousWeek
                daysAgo < 30 -> Recency.PreviousMonth
                else -> Recency.Older
            }
        }
        .map { (recency, grouped) -> SessionGroup(recency, grouped) }
}
