package com.abrarshakhi.mishti.domain.use_case

import com.abrarshakhi.mishti.domain.model.ChatMessage
import com.abrarshakhi.mishti.domain.repository.ConversationRepository
import com.abrarshakhi.mishti.llm.LlamaEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * Sends a user message and streams back the LLM reply token-by-token.
 *
 * Responsibilities:
 *  1. Persist the user message immediately.
 *  2. Build a chat-formatted prompt (simple <user>/<assistant> tags for now;
 *     swap for proper chat templates once the model picker is built).
 *  3. Stream tokens from [LlamaEngine].
 *  4. Persist the completed assistant message when the stream finishes.
 *
 * The ViewModel collects this flow to update the UI.
 */
class SendMessageUseCase(
    private val repository: ConversationRepository,
    private val engine: LlamaEngine,
) {
    /**
     * @param conversationId  Where to save the messages.
     * @param userText        Raw text the user typed.
     * @return                Flow of string tokens as the LLM generates them.
     */
    operator fun invoke(conversationId: String, userText: String): Flow<String> {
        val userMsg = ChatMessage(role = ChatMessage.Role.USER, text = userText)

        var fullReply = ""

        // We return a Flow. The caller (ViewModel) decides when to collect.
        // Persistence happens as side-effects on the flow operators.
        return engine.complete(buildPrompt(userText))
            .onEach { token -> fullReply += token }
            .onCompletion { cause ->
                // Only save if the stream completed naturally (not cancelled / errored).
                if (cause == null) {
                    repository.addMessage(conversationId, userMsg)
                    repository.addMessage(
                        conversationId,
                        ChatMessage(role = ChatMessage.Role.LLM, text = fullReply)
                    )

                    // Auto-title the conversation from the first user message.
                    if (userText.isNotBlank()) {
                        val title = userText.take(40).trimEnd()
                        repository.updateTitle(conversationId, title)
                    }
                }
            }
    }

    /**
     * Minimal prompt format — replace with a proper chat template
     * (e.g. ChatML, Llama-3 instruct) once model-specific config is in place.
     */
    private fun buildPrompt(userText: String): String =
        "<|user|>\n$userText\n<|assistant|>\n"
}
