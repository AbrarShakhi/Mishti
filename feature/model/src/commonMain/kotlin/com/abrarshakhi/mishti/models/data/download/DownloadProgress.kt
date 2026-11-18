package com.abrarshakhi.mishti.models.data.download

sealed interface DownloadProgress {
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : DownloadProgress
    data object Verifying : DownloadProgress
}
