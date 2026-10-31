package com.abrarshakhi.mishti.features.chat.presentation

import androidx.annotation.StringRes
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.features.chat.domain.model.ChatSession
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class Recency { Today, Yesterday, PreviousWeek, PreviousMonth, Older }

@get:StringRes
val Recency.labelRes: Int
    get() =
        when (this) {
            Recency.Today -> R.string.recency_today
            Recency.Yesterday -> R.string.recency_yesterday
            Recency.PreviousWeek -> R.string.recency_previous_week
            Recency.PreviousMonth -> R.string.recency_previous_month
            Recency.Older -> R.string.recency_older
        }

data class SessionGroup(val recency: Recency, val sessions: List<ChatSession>)

fun groupSessionsByRecency(sessions: List<ChatSession>, now: Long, zone: ZoneId): List<SessionGroup> {
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
        }.map { (recency, grouped) -> SessionGroup(recency, grouped) }
}
