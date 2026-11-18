package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WhistleSpeechRecognizerTest {
    @Test
    fun `confirmed deltas accumulate and pending words are replaced`() = runTest {
        val runtime = FakeRuntime(
            updates = listOf(update("Hello", "whirl"), update("world", "again"), update("", "again")),
            final = update("again"),
        )
        val recognizer = WhistleSpeechRecognizer(
            microphone = Microphone { flowOf(FloatArray(SpeechSampleRate * 2 + 400)) },
            runtime = runtime,
            readModel = { byteArrayOf(1) },
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        assertEquals(
            listOf(
                SpeechEvent.Listening,
                SpeechEvent.Preview("Hello whirl"),
                SpeechEvent.Preview("Hello world again"),
                SpeechEvent.Final("Hello world again"),
            ),
            recognizer.start().events.toList(),
        )
        assertEquals(listOf(SpeechSampleRate, SpeechSampleRate, 400), runtime.frameCounts)
        assertEquals(1, runtime.finishes)
    }

    @Test
    fun `silence reports no speech and resets the stream`() = runTest {
        val runtime = FakeRuntime(updates = listOf(update("")), final = update(""))
        val recognizer = WhistleSpeechRecognizer(
            microphone = Microphone { flowOf(FloatArray(SpeechSampleRate)) },
            runtime = runtime,
            readModel = { byteArrayOf(1) },
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val events = recognizer.start().events.toList()
        assertEquals(SpeechEvent.Failed(SpeechError.NoSpeech), events.last())
        assertEquals(1, runtime.finishes)
    }

    @Test
    fun `canceling capture closes the microphone and flushes an open stream once`() = runTest {
        var captureClosed = false
        val runtime = FakeRuntime(updates = listOf(update("First")), final = update("word"))
        val recognizer = WhistleSpeechRecognizer(
            microphone = Microphone {
                flow {
                    try {
                        emit(FloatArray(SpeechSampleRate))
                        awaitCancellation()
                    } finally {
                        captureClosed = true
                    }
                }
            },
            runtime = runtime,
            readModel = { byteArrayOf(1) },
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        val events = mutableListOf<SpeechEvent>()
        val job = launch { recognizer.start().events.toList(events) }
        runCurrent()
        job.cancelAndJoin()

        assertTrue(captureClosed)
        assertEquals(1, runtime.finishes)
        assertEquals(listOf(SpeechEvent.Listening, SpeechEvent.Preview("First")), events)
    }

    private class FakeRuntime(updates: List<String>, private val final: String) : WhistleRuntime {
        private val responses = ArrayDeque(updates)
        val frameCounts = mutableListOf<Int>()
        var finishes = 0
            private set

        override val isAvailable = true

        override fun load(model: ByteArray) = Unit

        override fun process(samples: FloatArray, count: Int): String {
            frameCounts.add(count)
            return responses.removeFirst()
        }

        override fun finish(): String {
            finishes++
            return final
        }
    }

    private fun update(text: String, pending: String = "") = """{"text":"$text","pending":"$pending"}"""
}
