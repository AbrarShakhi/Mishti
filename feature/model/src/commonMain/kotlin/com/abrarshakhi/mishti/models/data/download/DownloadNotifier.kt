package com.abrarshakhi.mishti.models.data.download

fun interface DownloadNotifier {
    fun onDownloadsActive()
    companion object {
        val Noop = DownloadNotifier { }
    }
}
