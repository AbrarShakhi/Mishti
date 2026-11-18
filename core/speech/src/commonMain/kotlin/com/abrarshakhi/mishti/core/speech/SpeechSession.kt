package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.Flow

interface SpeechSession {
    val events: Flow<SpeechEvent>

    fun stop()
}
