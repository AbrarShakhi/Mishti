package com.abrarshakhi.mishti.models.data

import okio.Source

/** Platform document access. Each opened source owns any security-scoped access until close. */
interface ModelImportSource {
    fun metadata(uri: String): ImportMetadata
    fun open(uri: String): Source
}

data class ImportMetadata(val name: String, val sizeBytes: Long)
