package com.abrarshakhi.mishti.models.data.download

data class DownloadRequest(val id: String, val name: String, val url: String, val sizeBytes: Long, val sha256: String)
