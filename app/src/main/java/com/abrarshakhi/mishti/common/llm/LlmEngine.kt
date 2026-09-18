package com.abrarshakhi.mishti.common.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface EngineState {
    data object Idle : EngineState

    data class Loading(val model: ModelHandle) : EngineState

    data class Ready(val model: ModelHandle) : EngineState

    data class Failed(val reason: String) : EngineState
}

data class LlmMessage(
    val role: LlmRole,
    val content: String,
)

enum class LlmRole { System, User, Assistant }

sealed interface GenerationEvent {
    data class Token(val text: String) : GenerationEvent

    data class Completed(
        val tokenCount: Int,
        val durationMillis: Long,
    ) : GenerationEvent
}

interface LlmEngine {

    val state: StateFlow<EngineState>

    suspend fun load(model: ModelHandle, options: EngineOptions = EngineOptions())

    suspend fun unload()

    fun generate(
        messages: List<LlmMessage>,
        params: GenerationParams = GenerationParams(),
    ): Flow<GenerationEvent>
}
