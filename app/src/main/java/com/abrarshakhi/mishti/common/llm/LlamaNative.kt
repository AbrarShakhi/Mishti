package com.abrarshakhi.mishti.common.llm


internal object LlamaNative {

    fun interface TokenCallback {
        fun onToken(piece: String): Boolean
    }

    val isAvailable: Boolean = runCatching { System.loadLibrary("mishti_llama") }.isSuccess

    external fun nativeInit()

    external fun nativeVersion(): String

    external fun nativeFree()

    external fun nativeLoadModel(path: String, contextTokens: Int, threads: Int): Long

    external fun nativeFreeModel(handle: Long)

    external fun nativeGenerate(
        handle: Long,
        roles: Array<String>,
        contents: Array<String>,
        temperature: Float,
        topK: Int,
        topP: Float,
        maxTokens: Int,
        callback: TokenCallback,
    ): Int
}
