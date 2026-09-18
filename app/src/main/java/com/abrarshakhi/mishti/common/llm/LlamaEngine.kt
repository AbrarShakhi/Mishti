package com.abrarshakhi.mishti.common.llm

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class LlamaEngine(
    private val inferenceDispatcher: CoroutineDispatcher = singleInferenceThread(),
) : LlmEngine {

    private val _state = MutableStateFlow<EngineState>(EngineState.Idle)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private var handle: Long = 0L

    private var loadedOptions: EngineOptions? = null

    override suspend fun load(model: ModelHandle, options: EngineOptions) {
        val current = _state.value
        if (current is EngineState.Ready && current.model.id == model.id && loadedOptions == options) {
            return
        }

        if (!LlamaNative.isAvailable) {
            _state.value = EngineState.Failed("Native library unavailable on this device.")
            return
        }

        _state.value = EngineState.Loading(model)
        withContext(inferenceDispatcher) {
            releaseLocked()
            LlamaNative.nativeInit()

            val loaded = runCatching {
                LlamaNative.nativeLoadModel(model.path, options.contextTokens, options.threads)
            }.getOrElse { error ->
                Log.e(TAG, "load threw", error)
                0L
            }

            if (loaded == 0L) {
                loadedOptions = null
                _state.value = EngineState.Failed("Could not load ${model.name}.")
            } else {
                handle = loaded
                loadedOptions = options
                _state.value = EngineState.Ready(model)
            }
        }
    }

    override suspend fun unload() {
        withContext(inferenceDispatcher) { releaseLocked() }
        _state.value = EngineState.Idle
    }

    override fun generate(
        messages: List<LlmMessage>,
        params: GenerationParams,
    ): Flow<GenerationEvent> = callbackFlow {
        val session = handle
        check(session != 0L) { "No model is loaded" }

        val startedAt = System.currentTimeMillis()
        val roles = messages.map { it.role.wireName() }.toTypedArray()
        val contents = messages.map { it.content }.toTypedArray()

        val produced = LlamaNative.nativeGenerate(
            session,
            roles,
            contents,
            params.temperature,
            params.topK,
            params.topP,
            params.maxTokens,
        ) { piece ->
            trySend(GenerationEvent.Token(piece)).isSuccess
        }

        if (produced < 0) {
            close(IllegalStateException("Generation failed."))
            return@callbackFlow
        }

        send(
            GenerationEvent.Completed(
                tokenCount = produced,
                durationMillis = System.currentTimeMillis() - startedAt,
            )
        )
        close()
        awaitClose { }
    }.flowOn(inferenceDispatcher)

    private fun releaseLocked() {
        if (handle != 0L) {
            LlamaNative.nativeFreeModel(handle)
            handle = 0L
        }
        loadedOptions = null
    }

    private fun LlmRole.wireName(): String = when (this) {
        LlmRole.System -> "system"
        LlmRole.User -> "user"
        LlmRole.Assistant -> "assistant"
    }

    companion object {
        private const val TAG = "MishtiLlama"

        fun defaultThreads(): Int = (Runtime.getRuntime().availableProcessors() / 2).coerceIn(2, 6)

        private fun singleInferenceThread(): CoroutineDispatcher =
            Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "mishti-inference").apply { isDaemon = true }
            }.asCoroutineDispatcher()
    }
}
