package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.Flow

interface SpeechRecognizer {
    val isSupported: Boolean

    fun start(): SpeechSession
}
