package com.abrarshakhi.mishti.core.speech

import kotlinx.coroutines.flow.Flow

enum class SpeechError {
    Microphone,
    Recognition,
    NoSpeech,
}
