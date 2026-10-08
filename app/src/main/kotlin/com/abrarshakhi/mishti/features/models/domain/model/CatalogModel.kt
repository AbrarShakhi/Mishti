package com.abrarshakhi.mishti.features.models.domain.model

enum class CatalogSource { MistirBhandar, PocketPal }

enum class CatalogProblem { Offline, Unreadable, Unavailable }

data class CatalogModel(
    val id: String,
    val source: CatalogSource,
    val name: String,
    val description: String?,
    val publisher: String?,
    val parametersLabel: String?,
    val quantization: String,
    val hfRepo: String,
    val hfFile: String,
    val sizeBytes: Long,
    val sha256: String,
    val requiredRamBytes: Long,
    val contextLength: Int?,
    val license: String?,
    val tags: Set<String>,
    val groupId: String?,
    val typicalTokensPerSecond: Double? = null,
) {
    val downloadUrl: String get() = "https://huggingface.co/$hfRepo/resolve/main/$hfFile"

    val pageUrl: String get() = "https://huggingface.co/$hfRepo"

    fun isSameFileAs(repo: String?, file: String?): Boolean = repo == hfRepo && file == hfFile
}

data class CatalogGroup(val id: String)

data class Catalog(
    val source: CatalogSource,
    val version: String,
    val models: List<CatalogModel>,
    val groups: List<CatalogGroup> = emptyList(),
    val recommendedIds: List<String> = emptyList(),
    val deviceGroupId: String? = null,
)

sealed interface CatalogState {
    val isRefreshing: Boolean

    data class Loading(override val isRefreshing: Boolean = true) : CatalogState

    data class Ready(
        val catalog: Catalog,
        val updatedAtMillis: Long?,
        val isOffline: Boolean,
        override val isRefreshing: Boolean,
    ) : CatalogState

    data class Unavailable(
        val problem: CatalogProblem,
        override val isRefreshing: Boolean,
    ) : CatalogState
}
