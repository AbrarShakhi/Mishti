package com.abrarshakhi.mishti.features.models.domain.model

enum class ModelOrigin { MishtirBhandar, PocketPal, Imported }

enum class TransferError {
    NotEnoughSpace,
    VerificationFailed,
    ServerError,
    Network,
    CannotOpen,
    NotGguf,
    CannotSave,
    Unknown,
}

fun CatalogSource.toOrigin(): ModelOrigin =
    when (this) {
        CatalogSource.MishtirBhandar -> ModelOrigin.MishtirBhandar
        CatalogSource.PocketPal -> ModelOrigin.PocketPal
    }

data class ShelfModel(
    val id: String,
    val name: String,
    val origin: ModelOrigin,
    val quantization: String?,
    val parametersLabel: String?,
    val sizeBytes: Long,
    val hfRepo: String?,
    val hfFile: String?,
    val architecture: String?,
    val contextLength: Int?,
    val license: String?,
    val installedAtMillis: Long,
)

sealed interface TransferStatus {
    data class Downloading(
        val doneBytes: Long,
        val totalBytes: Long,
    ) : TransferStatus {
        val fraction: Float
            get() = if (totalBytes <= 0L) 0f else (doneBytes.toFloat() / totalBytes).coerceIn(
                0f,
                1f
            )
    }

    data object Verifying : TransferStatus

    data class Importing(
        val doneBytes: Long,
        val totalBytes: Long,
    ) : TransferStatus {
        val fraction: Float
            get() = if (totalBytes <= 0L) 0f else (doneBytes.toFloat() / totalBytes).coerceIn(
                0f,
                1f
            )
    }

    data class Failed(
        val error: TransferError,
    ) : TransferStatus
}

data class Transfer(
    val id: String,
    val name: String,
    val status: TransferStatus,
) {
    val isActive: Boolean get() = status !is TransferStatus.Failed
}
