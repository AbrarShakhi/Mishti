package com.abrarshakhi.mishti.common.llm

data class GenerationParams(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 512,
)

data class EngineOptions(
    val contextTokens: Int = 2048,
    val threads: Int = 4,
)

data class InferenceSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 512,
    val contextTokens: Int = 2048,
    val threads: Int = 4,
) {
    val generation: GenerationParams
        get() = GenerationParams(temperature, topK, topP, maxTokens)

    val engine: EngineOptions
        get() = EngineOptions(contextTokens, threads)

    companion object {
        val TemperatureRange = 0.1f..1.5f
        val TopPRange = 0.1f..1.0f
        val TopKRange = 1..100
        val MaxTokensRange = 64..2048
        val ContextRange = 512..8192
        val ThreadsRange = 1..8
    }
}
