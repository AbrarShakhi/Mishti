package com.abrarshakhi.mishti.core.llm.data

import com.abrarshakhi.mishti.core.llm.domain.LlamaBridge

class AndroidLlamaBridge : LlamaBridge {
    override val isAvailable: Boolean
        get() = LlamaNative.isAvailable

    override fun init() = LlamaNative.nativeInit()

    override fun version(): String = LlamaNative.nativeVersion()

    override fun free() = LlamaNative.nativeFree()

    override fun loadModel(path: String, contextTokens: Int, threads: Int): Long =
        LlamaNative.nativeLoadModel(path, contextTokens, threads)

    override fun freeModel(handle: Long) = LlamaNative.nativeFreeModel(handle)

    override fun chatTemplate(handle: Long): String? = LlamaNative.nativeChatTemplate(handle)

    override fun generate(
        handle: Long,
        roles: Array<String>,
        contents: Array<String>,
        temperature: Float,
        topK: Int,
        topP: Float,
        maxTokens: Int,
        assistantPrefix: String,
        callback: (String) -> Boolean,
    ): Int = LlamaNative.nativeGenerate(
        handle = handle,
        roles = roles,
        contents = contents,
        temperature = temperature,
        topK = topK,
        topP = topP,
        maxTokens = maxTokens,
        assistantPrefix = assistantPrefix,
        callback = callback,
    )
}
