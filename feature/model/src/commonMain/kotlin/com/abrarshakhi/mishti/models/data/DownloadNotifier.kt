package com.abrarshakhi.mishti.models.data

fun interface DownloadNotifier {
    fun onDownloadsActive()
    companion object {
        val Noop = DownloadNotifier { }
    }
}
