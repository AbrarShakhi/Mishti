package com.abrarshakhi.mishti.chat.domain.usecase

import com.abrarshakhi.mishti.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.chat.domain.repository.ChatRepository

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
