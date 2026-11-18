package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.flowOf

fun createSpeechRecognizer(): SpeechRecognizer = UnavailableIosRecognizer

private object UnavailableIosRecognizer : SpeechRecognizer {
    override val isSupported = false

    override fun start(): SpeechSession = object : SpeechSession {
        override val events = flowOf(SpeechEvent.Failed(SpeechError.Recognition))

        override fun stop() = Unit
    }
}
