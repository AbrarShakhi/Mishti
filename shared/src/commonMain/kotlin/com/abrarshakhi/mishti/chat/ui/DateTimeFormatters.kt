package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatSession

expect fun formatMessageTime(millis: Long): String

expect fun groupSessionsByRecency(sessions: List<ChatSession>, now: Long): List<SessionGroup>
