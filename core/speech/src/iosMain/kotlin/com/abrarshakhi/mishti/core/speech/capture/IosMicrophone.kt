@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.abrarshakhi.mishti.core.speech.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioApplication
import platform.AVFAudio.AVAudioConverter
import platform.AVFAudio.AVAudioConverterInputStatus_HaveData
import platform.AVFAudio.AVAudioConverterInputStatus_NoDataNow
import platform.AVFAudio.AVAudioConverterOutputStatus_Error
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.setActive
import platform.Foundation.NSNotificationCenter
import kotlin.math.ceil

internal object IosMicrophone : Microphone {
    override fun record(stop: StateFlow<Boolean>) = callbackFlow {
        if (stop.value) {
            close()
            return@callbackFlow
        }
        val session = AVAudioSession.sharedInstance()
        check(session.setCategory(AVAudioSessionCategoryRecord, error = null))
        check(session.setActive(true, error = null))

        val engine = AVAudioEngine()
        val input = engine.inputNode
        val notifications = NSNotificationCenter.defaultCenter
        val interruption = notifications.addObserverForName(
            AVAudioSessionInterruptionNotification,
            `object` = session,
            queue = null,
        ) { close(IllegalStateException("Microphone interrupted")) }
        var tapInstalled = false
        try {
            val inputFormat = input.outputFormatForBus(0uL)
            check(inputFormat.sampleRate > 0 && inputFormat.channelCount > 0u)
            val outputFormat = AVAudioFormat(AVAudioPCMFormatFloat32, SpeechSampleRate.toDouble(), 1u, false)
            val converter = AVAudioConverter(inputFormat, outputFormat)

            input.installTapOnBus(0uL, bufferSize = 1024u, format = inputFormat) { buffer, _ ->
                buffer ?: return@installTapOnBus
                val samples = runCatching { converter.convertSamples(buffer, outputFormat) }.getOrElse {
                    close(it)
                    return@installTapOnBus
                }
                if (samples.isNotEmpty()) {
                    val sent = trySend(samples)
                    if (sent.isFailure && !sent.isClosed) close(IllegalStateException("Microphone buffer is full"))
                }
            }
            tapInstalled = true
            engine.prepare()
            check(engine.startAndReturnError(null))
            val stopListener = launch {
                stop.first { it }
                close()
            }
            try {
                awaitClose()
            } finally {
                stopListener.cancel()
            }
        } finally {
            engine.stop()
            if (tapInstalled) input.removeTapOnBus(0uL)
            notifications.removeObserver(interruption)
            session.setActive(false, error = null)
        }
    }.flowOn(Dispatchers.Main)
}

private fun AVAudioConverter.convertSamples(input: AVAudioPCMBuffer, format: AVAudioFormat): FloatArray {
    val frames = ceil(input.frameLength.toDouble() * format.sampleRate / input.format.sampleRate).toUInt()
    val output = AVAudioPCMBuffer(format, frames + 32u)
    var supplied = false
    val status = convertToBuffer(output, error = null) { _, inputStatus ->
        if (supplied) {
            inputStatus?.pointed?.value = AVAudioConverterInputStatus_NoDataNow
            null
        } else {
            supplied = true
            inputStatus?.pointed?.value = AVAudioConverterInputStatus_HaveData
            input
        }
    }
    check(status != AVAudioConverterOutputStatus_Error)
    val channel = output.floatChannelData?.get(0) ?: return FloatArray(0)
    return FloatArray(output.frameLength.toInt()) { channel[it] }
}
