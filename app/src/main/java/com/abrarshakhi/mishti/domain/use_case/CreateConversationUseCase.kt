package com.abrarshakhi.mishti.domain.use_case

import com.abrarshakhi.mishti.domain.repository.ConversationRepository

/**
 * Creates a new conversation for the given model and returns its id.
 * Encapsulates the creation logic so the ViewModel stays thin.
 */
class CreateConversationUseCase(
    private val repository: ConversationRepository,
) {
    suspend operator fun invoke(modelId: String): String =
        repository.create(modelId)
}
