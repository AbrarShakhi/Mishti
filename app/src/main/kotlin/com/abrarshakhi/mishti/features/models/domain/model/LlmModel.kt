package com.abrarshakhi.mishti.features.models.domain.model


data class LlmModel(
    val id: String,
    val name: String,
    val description: String,
    val parameters: String,
    val quantization: String,
    val sizeBytes: Long,
    val url: String,
    val sha256: String,
    val minRamBytes: Long,
)

sealed interface ModelStatus {

    data object NotDownloaded : ModelStatus

    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : ModelStatus {
        val fraction: Float
            get() = if (totalBytes <= 0L) 0f else (downloadedBytes.toFloat() / totalBytes)
    }

    data object Verifying : ModelStatus

    data class Downloaded(val sizeOnDiskBytes: Long) : ModelStatus

    data class Failed(val reason: String) : ModelStatus
}

data class ModelEntry(
    val model: LlmModel,
    val status: ModelStatus,
) {
    val isDownloaded: Boolean get() = status is ModelStatus.Downloaded
    val isBusy: Boolean get() = status is ModelStatus.Downloading || status is ModelStatus.Verifying
}
