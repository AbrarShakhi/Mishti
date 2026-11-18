package com.abrarshakhi.mishti.core.speech

import com.abrarshakhi.mishti.core.speech.capture.IosMicrophone
import com.abrarshakhi.mishti.core.speech.whistle.IosWhistleRuntime
import com.abrarshakhi.mishti.core.speech.whistle.WhistleSpeechRecognizer
import com.abrarshakhi.mishti.core.speech.whistle.readWhistleModel

fun createSpeechRecognizer(): SpeechRecognizer = WhistleSpeechRecognizer(
    microphone = IosMicrophone,
    runtime = IosWhistleRuntime,
    readModel = ::readWhistleModel,
)
