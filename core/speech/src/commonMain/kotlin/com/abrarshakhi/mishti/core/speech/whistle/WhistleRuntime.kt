package com.abrarshakhi.mishti.core.speech.whistle

import com.abrarshakhi.mishti.core.speech.resources.Res

internal const val WhistleOutputCapacity = 65_536

internal suspend fun readWhistleModel(): ByteArray = Res.readBytes("files/whistle/whistle.cact")

internal interface WhistleRuntime {
    val isAvailable: Boolean

    fun load(model: ByteArray)

    fun process(samples: FloatArray, count: Int): String

    fun finish(): String
}

internal fun ByteArray.decodeWhistleResult(code: Int): String {
    check(code >= 0)
    val end = indexOf(0)
    check(end > 0)
    return decodeToString(endIndex = end, throwOnInvalidSequence = true)
}
