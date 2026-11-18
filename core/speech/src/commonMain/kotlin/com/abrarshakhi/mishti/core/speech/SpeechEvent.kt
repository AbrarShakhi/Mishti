package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.Flow

sealed interface SpeechEvent {
    data object Listening : SpeechEvent

    data class Preview(val text: String) : SpeechEvent

    data class Final(val text: String) : SpeechEvent

    data class Failed(val error: SpeechError) : SpeechEvent
}
