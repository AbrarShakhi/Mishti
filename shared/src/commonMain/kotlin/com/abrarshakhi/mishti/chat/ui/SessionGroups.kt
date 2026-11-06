package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.recency_older
import com.abrarshakhi.mishti.shared.resources.recency_previous_month
import com.abrarshakhi.mishti.shared.resources.recency_previous_week
import com.abrarshakhi.mishti.shared.resources.recency_today
import com.abrarshakhi.mishti.shared.resources.recency_yesterday
import org.jetbrains.compose.resources.StringResource

enum class Recency { Today, Yesterday, PreviousWeek, PreviousMonth, Older }

val Recency.labelRes: StringResource
    get() = when (this) {
        Recency.Today -> Res.string.recency_today
        Recency.Yesterday -> Res.string.recency_yesterday
        Recency.PreviousWeek -> Res.string.recency_previous_week
        Recency.PreviousMonth -> Res.string.recency_previous_month
        Recency.Older -> Res.string.recency_older
    }

data class SessionGroup(val recency: Recency, val sessions: List<ChatSession>)
