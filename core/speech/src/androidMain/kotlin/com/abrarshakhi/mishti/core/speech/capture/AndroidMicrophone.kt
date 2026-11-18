package com.abrarshakhi.mishti.core.speech.capture

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

internal object AndroidMicrophone : Microphone {
    @SuppressLint("MissingPermission")
    override fun record(stop: StateFlow<Boolean>) = channelFlow {
        if (stop.value) return@channelFlow
        val channels = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_FLOAT
        val minimum = AudioRecord.getMinBufferSize(SpeechSampleRate, channels, encoding)
        check(minimum > 0)
        val buffer = FloatArray(SpeechSampleRate / 20)
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SpeechSampleRate)
                    .setChannelMask(channels)
                    .setEncoding(encoding)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(minimum, 4 * buffer.size * Float.SIZE_BYTES))
            .build()
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED)
            recorder.startRecording()
            check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING)
            while (!stop.value) {
                val read = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
                check(read >= 0)
                if (read == 0) {
                    delay(10)
                } else {
                    val sent = trySend(buffer.copyOf(read))
                    if (sent.isClosed) break
                    check(sent.isSuccess)
                }
            }
        } finally {
            try {
                if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop()
            } finally {
                recorder.release()
            }
        }
    }.flowOn(Dispatchers.IO)
}
