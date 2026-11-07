package com.abrarshakhi.mishti.core.session.ui

import com.abrarshakhi.mishti.core.domain.chat.model.ChatSession

expect fun groupSessionsByRecency(sessions: List<ChatSession>, now: Long): List<SessionGroup>
