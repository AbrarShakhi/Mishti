package com.abrarshakhi.mishti.core.session.ui

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession
import com.abrarshakhi.mishti.core.session.resources.recency_older
import com.abrarshakhi.mishti.core.session.resources.recency_previous_month
import com.abrarshakhi.mishti.core.session.resources.recency_previous_week
import com.abrarshakhi.mishti.core.session.resources.recency_today
import com.abrarshakhi.mishti.core.session.resources.recency_yesterday
import org.jetbrains.compose.resources.StringResource
import com.abrarshakhi.mishti.core.session.resources.Res as CoreSessionRes

enum class Recency { Today, Yesterday, PreviousWeek, PreviousMonth, Older }

val Recency.labelRes: StringResource
    get() = when (this) {
        Recency.Today -> CoreSessionRes.string.recency_today
        Recency.Yesterday -> CoreSessionRes.string.recency_yesterday
        Recency.PreviousWeek -> CoreSessionRes.string.recency_previous_week
        Recency.PreviousMonth -> CoreSessionRes.string.recency_previous_month
        Recency.Older -> CoreSessionRes.string.recency_older
    }

data class SessionGroup(val recency: Recency, val sessions: List<ChatSession>)
