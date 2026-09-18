package com.abrarshakhi.mishti.common.llm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class ScriptedLlmEngine(
    private val script: List<String> = DEFAULT_SCRIPT,
    private val loadDelayMillis: Long = 900L,
    private val tokenDelayMillis: Long = 45L,
    private val clock: () -> Long = System::currentTimeMillis,
) : LlmEngine {

    private val _state = MutableStateFlow<EngineState>(EngineState.Idle)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    override suspend fun load(model: ModelHandle, options: EngineOptions) {
        val current = _state.value
        if (current is EngineState.Ready && current.model.id == model.id) return
        _state.value = EngineState.Loading(model)
        delay(loadDelayMillis.milliseconds)
        _state.value = EngineState.Ready(model)
    }

    override suspend fun unload() {
        _state.value = EngineState.Idle
    }

    override fun generate(
        messages: List<LlmMessage>,
        params: GenerationParams,
    ): Flow<GenerationEvent> = flow {
        check(_state.value is EngineState.Ready) { "No model is loaded" }

        val startedAt = clock()
        val reply = script[messages.count { it.role == LlmRole.User } % script.size]

        var emitted = 0
        for (chunk in reply.chunkedForStreaming()) {
            delay(tokenDelayMillis.milliseconds)
            emit(GenerationEvent.Token(chunk))
            emitted++
        }

        emit(
            GenerationEvent.Completed(
                tokenCount = emitted,
                durationMillis = clock() - startedAt,
            )
        )
    }

    private fun String.chunkedForStreaming(): List<String> {
        val out = mutableListOf<String>()
        var index = 0
        while (index < length) {
            val size = if (index % 2 == 0) 3 else 4
            val end = minOf(index + size, length)
            out += substring(index, end)
            index = end
        }
        return out
    }

    private companion object {
        val DEFAULT_SCRIPT = listOf(
            "I am a placeholder. No model is running yet — this reply is scripted so the " + "streaming UI can be built and tested before the engine is wired up.",
            "Still scripted. Cancelling mid-reply works: whatever has been generated so far " + "is kept, exactly as it will be with a real model.",
            "Once an inference backend is bound behind LlmEngine, these canned answers go " + "away and nothing above this seam has to change.",
        )
    }
}
