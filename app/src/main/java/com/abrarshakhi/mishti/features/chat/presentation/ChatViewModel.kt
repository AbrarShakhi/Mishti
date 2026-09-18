package com.abrarshakhi.mishti.features.chat.presentation

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.GenerationEvent
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.llm.LlmMessage
import com.abrarshakhi.mishti.common.llm.LlmRole
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.features.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class ChatViewModel(
    private val requestedSessionId: String?,
    private val repository: ChatRepository,
    private val engine: LlmEngine,
    private val preferences: AppPreferences,
    private val clock: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : MviViewModel<ChatUiState, ChatIntent, ChatEffect>(ChatUiState()) {

    private var settings: InferenceSettings = InferenceSettings()

    private var generationJob: Job? = null

    init {
        viewModelScope.launch { openSession() }
        viewModelScope.launch {
            preferences.inferenceSettings.collect { settings = it }
        }
        viewModelScope.launch {
            engine.state.collect { engineState ->
                updateState {
                    copy(
                        engineState = engineState,
                        canSend = draft.isNotBlank() && !isGenerating && engineState.isReady(),
                    )
                }
            }
        }
    }

    private suspend fun openSession() {
        val sessionId = try {
            requestedSessionId ?: repository.latestSessionId() ?: repository.createSession(
                UNTITLED_SESSION
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            updateState { copy(isLoading = false) }
            emitEffect(ChatEffect.ShowError("Could not open this conversation."))
            return
        }

        updateState { copy(sessionId = sessionId) }

        repository.observeMessages(sessionId).collect { messages ->
            updateState { copy(messages = messages, isLoading = false) }
        }
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.DraftChanged -> onDraftChanged(intent.text)
            ChatIntent.SendClicked -> onSendClicked()
            ChatIntent.StopClicked -> onStopClicked()
        }
    }

    private fun onDraftChanged(text: String) = updateState {
        copy(draft = text, canSend = text.isNotBlank() && !isGenerating && engineState.isReady())
    }

    private fun onSendClicked() {
        if (!currentState.canSend) return
        val sessionId = currentState.sessionId ?: return

        val message = ChatMessage(
            id = newId(),
            author = MessageAuthor.User,
            content = currentState.draft.trim(),
            createdAtMillis = clock(),
        )

        val history = currentState.messages + message

        updateState { copy(draft = "", canSend = false) }

        viewModelScope.launch {
            try {
                repository.appendMessage(sessionId, message)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                updateState { copy(draft = message.content, canSend = true) }
                emitEffect(ChatEffect.ShowError("Could not send that message."))
                return@launch
            }
            generateReply(sessionId, history)
        }
    }

    private fun onStopClicked() {
        generationJob?.cancel()
    }

    private fun generateReply(sessionId: String, history: List<ChatMessage>) {
        generationJob = viewModelScope.launch {
            val reply = StringBuilder()
            updateState { copy(isGenerating = true, canStop = true, streamingResponse = "") }

            var throughput: Double? = null
            try {
                val prompt = buildList {
                    val instruction = settings.systemPrompt.trim()
                    if (instruction.isNotEmpty()) {
                        add(LlmMessage(LlmRole.System, instruction))
                    }
                    addAll(history.map { it.toLlmMessage() })
                }

                engine.generate(prompt, settings.generation).collect { event ->
                    when (event) {
                        is GenerationEvent.Token -> {
                            reply.append(event.text)
                            updateState { copy(streamingResponse = reply.toString()) }
                        }

                        is GenerationEvent.Completed -> {
                            throughput = event.tokensPerSecond()
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emitEffect(ChatEffect.ShowError("Generation failed."))
            } finally {
                withContext(NonCancellable) {
                    persistReply(sessionId, reply.toString(), throughput)
                }
            }
        }
    }

    private suspend fun persistReply(
        sessionId: String,
        text: String,
        tokensPerSecond: Double?,
    ) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            val message = ChatMessage(
                id = newId(),
                author = MessageAuthor.Assistant,
                content = trimmed,
                createdAtMillis = clock(),
                tokensPerSecond = tokensPerSecond,
            )
            runCatching { repository.appendMessage(sessionId, message) }
        }
        updateState {
            copy(
                streamingResponse = "",
                isGenerating = false,
                canStop = false,
                canSend = draft.isNotBlank() && engineState.isReady(),
            )
        }
    }
}

private fun EngineState.isReady() = this is EngineState.Ready

private fun GenerationEvent.Completed.tokensPerSecond(): Double? =
    if (durationMillis > 0 && tokenCount > 0) tokenCount * 1000.0 / durationMillis else null

private fun ChatMessage.toLlmMessage() = LlmMessage(
    role = when (author) {
        MessageAuthor.User -> LlmRole.User
        MessageAuthor.Assistant -> LlmRole.Assistant
    },
    content = content,
)
