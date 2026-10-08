package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.text.uiText
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.GenerationEvent
import com.abrarshakhi.mishti.common.llm.GenerationParams
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.llm.LlmMessage
import com.abrarshakhi.mishti.common.llm.LlmRole
import com.abrarshakhi.mishti.common.llm.ReasoningSplit
import com.abrarshakhi.mishti.common.llm.splitReasoning
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.features.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
            combine(engine.state, preferences.thinkingModelIds, ::Pair).collect { (engineState, thinkingIds) ->
                val ready = engineState as? EngineState.Ready
                updateState {
                    copy(
                        engineState = engineState,
                        canSend = draft.isNotBlank() && !isGenerating && engineState.isReady(),
                        thinkingSupported = ready?.supportsThinking == true,
                        thinkingEnabled = ready != null && ready.model.id in thinkingIds,
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
            emitEffect(ChatEffect.ShowError(uiText(R.string.chat_error_open)))
            return
        }

        updateState { copy(sessionId = sessionId) }

        viewModelScope.launch { observeTitle(sessionId) }

        repository.observeMessages(sessionId).collect { messages ->
            updateState { copy(messages = messages, isLoading = false) }
        }
    }

    private suspend fun observeTitle(sessionId: String) {
        repository.observeSessions()
            .map { sessions -> sessions.firstOrNull { it.id == sessionId }?.title.orEmpty() }
            .distinctUntilChanged()
            .collect { title -> updateState { copy(title = title) } }
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.DraftChanged -> onDraftChanged(intent.text)
            ChatIntent.SendClicked -> onSendClicked()
            ChatIntent.StopClicked -> onStopClicked()
            ChatIntent.ThinkingToggled -> onThinkingToggled()
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
                emitEffect(ChatEffect.ShowError(uiText(R.string.chat_error_send)))
                return@launch
            }
            generateReply(sessionId, history)
        }
    }

    private fun onStopClicked() {
        generationJob?.cancel()
    }

    private fun onThinkingToggled() {
        val ready = currentState.engineState as? EngineState.Ready ?: return
        if (!ready.supportsThinking) return
        val enabled = !currentState.thinkingEnabled
        viewModelScope.launch {
            try {
                preferences.setThinking(ready.model.id, enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emitEffect(ChatEffect.ShowError(uiText(R.string.settings_save_failed)))
            }
        }
    }

    private fun generateReply(sessionId: String, history: List<ChatMessage>) {
        val splitsReasoning = currentState.thinkingSupported
        val thinking = splitsReasoning && currentState.thinkingEnabled
        generationJob = viewModelScope.launch {
            val reply = ReplyBuffer(splitsReasoning, clock)
            updateState {
                copy(
                    isGenerating = true,
                    canStop = true,
                    streamingResponse = "",
                    streamingReasoning = null,
                    isReasoning = false,
                    reasoningMillis = null,
                )
            }

            var throughput: Double? = null
            try {
                val prompt = buildList {
                    val instruction = settings.systemPrompt.trim()
                    if (instruction.isNotEmpty()) {
                        add(LlmMessage(LlmRole.System, instruction))
                    }
                    addAll(history.filter { it.content.isNotBlank() }.map { it.toLlmMessage() })
                }

                engine.generate(prompt, settings.generation.withThinking(thinking)).collect { event ->
                    when (event) {
                        is GenerationEvent.Token -> {
                            val split = reply.append(event.text)
                            updateState {
                                copy(
                                    streamingResponse = split.answer,
                                    streamingReasoning = split.reasoning,
                                    isReasoning = split.isReasoning,
                                    reasoningMillis = reply.reasoningMillis,
                                )
                            }
                        }

                        is GenerationEvent.Completed -> {
                            throughput = event.tokensPerSecond()
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emitEffect(ChatEffect.ShowError(uiText(R.string.chat_error_generation)))
            } finally {
                withContext(NonCancellable) {
                    persistReply(sessionId, reply, throughput)
                }
            }
        }
    }

    private suspend fun persistReply(
        sessionId: String,
        reply: ReplyBuffer,
        tokensPerSecond: Double?,
    ) {
        val finished = reply.finish()
        val content = finished.answer.trim()
        val reasoning = finished.reasoning?.takeIf { it.isNotBlank() }
        if (content.isNotEmpty() || reasoning != null) {
            val message = ChatMessage(
                id = newId(),
                author = MessageAuthor.Assistant,
                content = content,
                createdAtMillis = clock(),
                tokensPerSecond = tokensPerSecond,
                reasoning = reasoning,
                reasoningMillis = reply.reasoningMillis.takeIf { reasoning != null },
            )
            runCatching { repository.appendMessage(sessionId, message) }
        }
        updateState {
            copy(
                streamingResponse = "",
                streamingReasoning = null,
                isReasoning = false,
                reasoningMillis = null,
                isGenerating = false,
                canStop = false,
                canSend = draft.isNotBlank() && engineState.isReady(),
            )
        }
    }
}

private class ReplyBuffer(
    private val splitsReasoning: Boolean,
    private val clock: () -> Long,
) {
    private val raw = StringBuilder()
    private var firstTokenAtMillis: Long? = null

    var reasoningMillis: Long? = null
        private set

    fun append(piece: String): ReasoningSplit {
        if (firstTokenAtMillis == null) firstTokenAtMillis = clock()
        raw.append(piece)
        val split = split(complete = false)
        if (reasoningMillis == null && split.reasoning != null && !split.isReasoning) {
            reasoningMillis = elapsedMillis()
        }
        return split
    }

    fun finish(): ReasoningSplit {
        val split = split(complete = true)
        if (reasoningMillis == null && split.reasoning != null) reasoningMillis = elapsedMillis()
        return split
    }

    private fun split(complete: Boolean): ReasoningSplit =
        if (splitsReasoning) splitReasoning(raw.toString(), complete)
        else ReasoningSplit(reasoning = null, answer = raw.toString(), isReasoning = false)

    private fun elapsedMillis(): Long? = firstTokenAtMillis?.let { clock() - it }
}

private const val ThinkingTokenFactor = 2

private fun GenerationParams.withThinking(enabled: Boolean) =
    if (enabled) copy(thinking = true, maxTokens = maxTokens * ThinkingTokenFactor) else this

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
