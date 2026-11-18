package com.abrarshakhi.mishti.core.speech

internal object WhistleNative {
    val isAvailable: Boolean = runCatching { System.loadLibrary("mishti_speech") }.isSuccess

    external fun load(model: ByteArray): Int

    external fun process(samples: FloatArray, count: Int, output: ByteArray): Int

    external fun stop(output: ByteArray): Int
}
