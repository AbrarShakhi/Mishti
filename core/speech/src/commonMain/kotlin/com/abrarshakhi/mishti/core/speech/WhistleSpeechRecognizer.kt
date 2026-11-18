package com.abrarshakhi.mishti.core.speech

import com.abrarshakhi.mishti.core.speech.resources.Res
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.seconds

internal const val SpeechSampleRate = 16_000
internal const val WhistleOutputCapacity = 65_536

internal suspend fun readWhistleModel(): ByteArray = Res.readBytes("files/whistle/whistle.cact")

internal fun interface Microphone {
    fun record(stop: StateFlow<Boolean>): Flow<FloatArray>
}

internal interface WhistleRuntime {
    val isAvailable: Boolean

    fun load(model: ByteArray)

    fun process(samples: FloatArray, count: Int): String

    fun finish(): String
}

internal class WhistleSpeechRecognizer(
    private val microphone: Microphone,
    private val runtime: WhistleRuntime,
    private val readModel: suspend () -> ByteArray,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpeechRecognizer {
    private val runtimeLock = Mutex()
    private var loaded = false

    override val isSupported: Boolean
        get() = runtime.isAvailable

    override fun start(): SpeechSession = Recording()

    private inner class Recording : SpeechSession {
        private val stopRequested = MutableStateFlow(false)

        override fun stop() {
            stopRequested.value = true
        }

        override val events: Flow<SpeechEvent> = flow {
            runtimeLock.withLock {
                check(isSupported)
                if (!loaded) {
                    runtime.load(readModel())
                    loaded = true
                }
                if (stopRequested.value) {
                    emit(SpeechEvent.Failed(SpeechError.NoSpeech))
                    return@withLock
                }

                val transcript = StringBuilder()
                val batch = FloatArray(SpeechSampleRate)
                var inBatch = 0
                var received = 0
                var streamOpen = false

                fun process(): String {
                    streamOpen = true
                    val pending = transcript.addResult(runtime.process(batch, inBatch))
                    inBatch = 0
                    return joinWords(transcript.toString(), pending)
                }

                try {
                    emit(SpeechEvent.Listening)
                    coroutineScope {
                        val timeout = launch {
                            delay(RecordingLimit.seconds)
                            stop()
                        }
                        try {
                            microphone.record(stopRequested)
                                .buffer(64)
                                .catch { throw MicrophoneFailure(it) }
                                .collect { samples ->
                                    var offset = 0
                                    val available = minOf(samples.size, MaxSamples - received)
                                    while (offset < available) {
                                        val count = minOf(available - offset, batch.size - inBatch)
                                        samples.copyInto(batch, inBatch, offset, offset + count)
                                        offset += count
                                        inBatch += count
                                        if (inBatch == batch.size) emit(SpeechEvent.Preview(process()))
                                    }
                                    received += available
                                    if (received == MaxSamples) stop()
                                }
                        } finally {
                            timeout.cancel()
                        }
                    }
                    if (inBatch > 0) process()
                    if (streamOpen) {
                        streamOpen = false
                        transcript.addResult(runtime.finish())
                    }
                    val text = transcript.toString()
                    emit(if (text.isBlank()) SpeechEvent.Failed(SpeechError.NoSpeech) else SpeechEvent.Final(text))
                } finally {
                    if (streamOpen) runCatching { runtime.finish() }
                }
            }
        }.catch { error ->
            val kind = if (error is MicrophoneFailure) SpeechError.Microphone else SpeechError.Recognition
            emit(SpeechEvent.Failed(kind))
        }.flowOn(dispatcher)
    }
}

private const val RecordingLimit = 30
private const val MaxSamples = RecordingLimit * SpeechSampleRate

private class MicrophoneFailure(cause: Throwable) : RuntimeException(cause)

private fun StringBuilder.addResult(response: String): String {
    val result = Json.parseToJsonElement(response).jsonObject
    val text = result.getValue("text").jsonPrimitive.content.trim()
    if (text.isNotEmpty()) {
        if (isNotEmpty()) append(' ')
        append(text)
    }
    return result.getValue("pending").jsonPrimitive.content.trim()
}

private fun joinWords(first: String, second: String): String = when {
    first.isEmpty() -> second
    second.isEmpty() -> first
    else -> "$first $second"
}

internal fun ByteArray.decodeWhistleResult(code: Int): String {
    check(code >= 0)
    val end = indexOf(0)
    check(end > 0)
    return decodeToString(endIndex = end, throwOnInvalidSequence = true)
}
