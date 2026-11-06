package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatMessageTime(millis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val isToday = calendar.isDateInToday(date)
    val formatter = NSDateFormatter().apply {
        dateStyle = if (isToday) NSDateFormatterNoStyle else NSDateFormatterShortStyle
        timeStyle = NSDateFormatterShortStyle
    }
    return formatter.stringFromDate(date)
}

actual fun groupSessionsByRecency(sessions: List<ChatSession>, now: Long): List<SessionGroup> {
    val calendar = NSCalendar.currentCalendar
    val nowDate = NSDate.dateWithTimeIntervalSince1970(now / 1000.0)

    return sessions
        .groupBy { session ->
            val sessionDate = NSDate.dateWithTimeIntervalSince1970(session.updatedAtMillis / 1000.0)
            val components = calendar.components(
                NSCalendarUnitDay,
                fromDate = sessionDate,
                toDate = nowDate,
                options = 0u,
            )
            val daysAgo = components.day
            when {
                daysAgo <= 0L -> Recency.Today
                daysAgo == 1L -> Recency.Yesterday
                daysAgo < 7L -> Recency.PreviousWeek
                daysAgo < 30L -> Recency.PreviousMonth
                else -> Recency.Older
            }
        }.map { (recency, grouped) -> SessionGroup(recency, grouped) }
}
