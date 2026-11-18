package com.abrarshakhi.mishti.core.speech.capture

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal const val SpeechSampleRate = 16_000

internal fun interface Microphone {
    fun record(stop: StateFlow<Boolean>): Flow<FloatArray>
}
