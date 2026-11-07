package com.abrarshakhi.mishti.core.llm.data

import com.abrarshakhi.mishti.core.domain.llm.EngineOptions
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.domain.llm.GenerationEvent
import com.abrarshakhi.mishti.core.domain.llm.GenerationParams
import com.abrarshakhi.mishti.core.domain.llm.LlamaBridge
import com.abrarshakhi.mishti.core.domain.llm.LlmEngine
import com.abrarshakhi.mishti.core.domain.llm.LlmMessage
import com.abrarshakhi.mishti.core.domain.llm.LlmRole
import com.abrarshakhi.mishti.core.domain.llm.ModelHandle
import com.abrarshakhi.mishti.core.domain.llm.SKIP_THINKING_PREFIX
import com.abrarshakhi.mishti.core.domain.llm.templateSupportsThinking
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class LlamaEngine(
    private val bridge: LlamaBridge,
    private val inferenceDispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1),
) : LlmEngine {
    private val _state = MutableStateFlow<EngineState>(EngineState.Idle)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private var handle: Long = 0L

    private var loadedOptions: EngineOptions? = null

    private var supportsThinking: Boolean = false

    override suspend fun load(model: ModelHandle, options: EngineOptions) {
        val current = _state.value
        if (current is EngineState.Ready && current.model.id == model.id &&
            loadedOptions == options
        ) {
            return
        }

        if (!bridge.isAvailable) {
            _state.value = EngineState.Failed("Native library unavailable on this device.")
            return
        }

        _state.value = EngineState.Loading(model)
        withContext(inferenceDispatcher) {
            releaseLocked()
            bridge.init()

            val loaded =
                runCatching {
                    bridge.loadModel(model.path, options.contextTokens, options.threads)
                }.getOrElse { error ->
                    println("[$TAG] load threw: $error")
                    0L
                }

            if (loaded == 0L) {
                loadedOptions = null
                _state.value = EngineState.Failed("Could not load ${model.name}.")
            } else {
                handle = loaded
                loadedOptions = options
                supportsThinking = bridge
                    .chatTemplate(loaded)
                    ?.let(::templateSupportsThinking) == true
                _state.value = EngineState.Ready(model, supportsThinking)
            }
        }
    }

    override suspend fun unload() {
        withContext(inferenceDispatcher) { releaseLocked() }
        _state.value = EngineState.Idle
    }

    override fun generate(messages: List<LlmMessage>, params: GenerationParams): Flow<GenerationEvent> = callbackFlow {
        val session = handle
        check(session != 0L) { "No model is loaded" }

        val startedAt = platformTimeMillis()
        val roles = messages.map { it.role.wireName() }.toTypedArray()
        val contents = messages.map { it.content }.toTypedArray()

        val produced =
            bridge.generate(
                session,
                roles,
                contents,
                params.temperature,
                params.topK,
                params.topP,
                params.maxTokens,
                if (supportsThinking && !params.thinking) SKIP_THINKING_PREFIX else "",
            ) { piece ->
                isActive && trySend(GenerationEvent.Token(piece)).isSuccess
            }

        if (produced < 0) {
            close(IllegalStateException("Generation failed."))
            return@callbackFlow
        }

        send(
            GenerationEvent.Completed(
                tokenCount = produced,
                durationMillis = platformTimeMillis() - startedAt,
            ),
        )
        close()
        awaitClose { }
    }.flowOn(inferenceDispatcher)

    private fun releaseLocked() {
        if (handle != 0L) {
            bridge.freeModel(handle)
            handle = 0L
        }
        loadedOptions = null
        supportsThinking = false
    }

    private fun LlmRole.wireName(): String = when (this) {
        LlmRole.System -> "system"
        LlmRole.User -> "user"
        LlmRole.Assistant -> "assistant"
    }

    companion object {
        private const val TAG = "MishtiLlama"

        fun defaultThreads(): Int = platformDefaultThreads()
    }
}
