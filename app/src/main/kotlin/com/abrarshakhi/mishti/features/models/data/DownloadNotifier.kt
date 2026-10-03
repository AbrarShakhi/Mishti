package com.abrarshakhi.mishti.features.models.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

fun interface DownloadNotifier {
    fun onDownloadsActive()

    companion object {
        val Noop = DownloadNotifier { }
    }
}

class ServiceDownloadNotifier(private val context: Context) : DownloadNotifier {

    override fun onDownloadsActive() {
        val intent = Intent(context, ModelDownloadService::class.java)
        runCatching { ContextCompat.startForegroundService(context, intent) }
    }
}
