package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

actual fun formatMessageTime(millis: Long): String {
    val date = Date(millis)
    val now = Calendar.getInstance()
    val msg = Calendar.getInstance().apply { time = date }
    val isToday = now.get(Calendar.YEAR) == msg.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)
    val pattern = if (isToday) "h:mm a" else "MMM d, h:mm a"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

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
