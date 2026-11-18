@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.abrarshakhi.mishti.core.speech

import com.abrarshakhi.mishti.core.speech.cinterop.cactus_load
import com.abrarshakhi.mishti.core.speech.cinterop.cactus_stream_transcribe_process
import com.abrarshakhi.mishti.core.speech.cinterop.cactus_stream_transcribe_stop
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned

fun createSpeechRecognizer(): SpeechRecognizer = WhistleSpeechRecognizer(
    microphone = IosMicrophone,
    runtime = IosWhistleRuntime,
    readModel = ::readWhistleModel,
)

private object IosWhistleRuntime : WhistleRuntime {
    override val isAvailable: Boolean = true

    override fun load(model: ByteArray) {
        model.usePinned { check(cactus_load(it.addressOf(0).reinterpret(), model.size.toULong()) >= 0) }
    }

    override fun process(samples: FloatArray, count: Int): String {
        val output = ByteArray(WhistleOutputCapacity)
        val code = output.usePinned { result ->
            samples.usePinned { pcm ->
                cactus_stream_transcribe_process(
                    pcm.addressOf(0),
                    count,
                    null,
                    null,
                    result.addressOf(0),
                    output.size,
                )
            }
        }
        return output.decodeWhistleResult(code)
    }

    override fun finish(): String {
        val output = ByteArray(WhistleOutputCapacity)
        val code = output.usePinned { cactus_stream_transcribe_stop(it.addressOf(0), output.size) }
        return output.decodeWhistleResult(code)
    }
}
