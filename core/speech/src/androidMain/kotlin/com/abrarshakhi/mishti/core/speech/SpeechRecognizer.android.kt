package com.abrarshakhi.mishti.core.speech

import com.abrarshakhi.mishti.core.speech.capture.AndroidMicrophone
import com.abrarshakhi.mishti.core.speech.whistle.AndroidWhistleRuntime
import com.abrarshakhi.mishti.core.speech.whistle.WhistleSpeechRecognizer
import com.abrarshakhi.mishti.core.speech.whistle.readWhistleModel

fun createSpeechRecognizer(): SpeechRecognizer = WhistleSpeechRecognizer(
    microphone = AndroidMicrophone,
    runtime = AndroidWhistleRuntime,
    readModel = ::readWhistleModel,
)
