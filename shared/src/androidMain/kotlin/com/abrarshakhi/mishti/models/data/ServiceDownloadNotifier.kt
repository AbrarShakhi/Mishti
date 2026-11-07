package com.abrarshakhi.mishti.models.data

import android.content.Context
import android.content.Intent

class ServiceDownloadNotifier(private val context: Context) : DownloadNotifier {
    override fun onDownloadsActive() {
        context.startForegroundService(Intent(context, ModelDownloadService::class.java))
    }
}
