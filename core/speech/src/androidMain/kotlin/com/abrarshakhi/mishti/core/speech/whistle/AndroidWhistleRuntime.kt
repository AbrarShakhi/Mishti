package com.abrarshakhi.mishti.core.speech.whistle

import com.abrarshakhi.mishti.core.speech.WhistleNative

internal object AndroidWhistleRuntime : WhistleRuntime {
    override val isAvailable: Boolean
        get() = WhistleNative.isAvailable

    override fun load(model: ByteArray) {
        check(WhistleNative.load(model) >= 0)
    }

    override fun process(samples: FloatArray, count: Int): String {
        val output = ByteArray(WhistleOutputCapacity)
        return output.decodeWhistleResult(WhistleNative.process(samples, count, output))
    }

    override fun finish(): String {
        val output = ByteArray(WhistleOutputCapacity)
        return output.decodeWhistleResult(WhistleNative.stop(output))
    }
}
