package com.abrarshakhi.mishti.core.session.ui

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

actual fun groupSessionsByRecency(sessions: List<ChatSession>, now: Long): List<SessionGroup> {
    val zone = ZoneId.systemDefault()
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()

    return sessions
        .groupBy { session ->
            val day = Instant.ofEpochMilli(session.updatedAtMillis).atZone(zone).toLocalDate()
            val daysAgo = ChronoUnit.DAYS.between(day, today)
            when {
                daysAgo <= 0L -> Recency.Today
                daysAgo == 1L -> Recency.Yesterday
                daysAgo < 7L -> Recency.PreviousWeek
                daysAgo < 30L -> Recency.PreviousMonth
                else -> Recency.Older
            }
        }.map { (recency, grouped) -> SessionGroup(recency, grouped) }
}
