package com.abrarshakhi.mishti.core.domain.chat.usecase

import com.abrarshakhi.mishti.core.domain.chat.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.core.domain.chat.repository.ChatRepository

class CreateNewSessionUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): String {
        val latest = repository.latestSessionId()
        return if (latest != null && repository.isSessionEmpty(latest)) {
            latest
        } else {
            repository.createSession(UNTITLED_SESSION)
        }
    }
}
