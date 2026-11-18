package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.Flow

interface SpeechRecognizer {
    val isSupported: Boolean

    fun start(): SpeechSession
}

interface SpeechSession {
    val events: Flow<SpeechEvent>

    fun stop()
}

sealed interface SpeechEvent {
    data object Listening : SpeechEvent

    data class Preview(val text: String) : SpeechEvent

    data class Final(val text: String) : SpeechEvent

    data class Failed(val error: SpeechError) : SpeechEvent
}

enum class SpeechError {
    Microphone,
    Recognition,
    NoSpeech,
}
