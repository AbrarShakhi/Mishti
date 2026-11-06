package com.abrarshakhi.mishti.core.llm.domain

interface LlamaBridge {
    val isAvailable: Boolean

    fun init()

    fun version(): String

    fun free()

    fun loadModel(path: String, contextTokens: Int, threads: Int): Long

    fun freeModel(handle: Long)

    fun chatTemplate(handle: Long): String?

    fun generate(
        handle: Long,
        roles: Array<String>,
        contents: Array<String>,
        temperature: Float,
        topK: Int,
        topP: Float,
        maxTokens: Int,
        assistantPrefix: String,
        callback: (String) -> Boolean,
    ): Int
}
