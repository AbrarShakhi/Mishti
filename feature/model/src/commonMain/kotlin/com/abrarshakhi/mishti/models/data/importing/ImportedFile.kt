package com.abrarshakhi.mishti.models.data.importing

import com.abrarshakhi.mishti.models.data.gguf.GgufInfo

data class ImportedFile(val displayName: String, val sizeBytes: Long, val info: GgufInfo)
