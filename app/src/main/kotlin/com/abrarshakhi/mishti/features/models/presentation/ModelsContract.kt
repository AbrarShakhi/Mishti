package com.abrarshakhi.mishti.features.models.presentation

import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.mvi.UiEffect
import com.abrarshakhi.mishti.common.mvi.UiIntent
import com.abrarshakhi.mishti.common.mvi.UiState
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage

enum class CatalogFilter { All, Fast, Balanced, Smart, Coding }

data class ModelsUiState(
    val isLoading: Boolean = true,
    val shelf: List<ShelfModel> = emptyList(),
    val transfers: List<Transfer> = emptyList(),
    val storage: StorageUsage = StorageUsage(),
    val capability: DeviceCapability = DeviceCapability.Unknown,
    val selectedModelId: String? = null,
    val catalogSource: CatalogSource = CatalogSource.MishtirBhandar,
    val catalogs: Map<CatalogSource, CatalogState> = emptyMap(),
    val filter: CatalogFilter = CatalogFilter.All,
    val query: String = "",
    val details: CatalogModel? = null,
    val deleting: ShelfModel? = null,
) : UiState {
    val activeCatalog: CatalogState
        get() = catalogs[catalogSource] ?: CatalogState.Loading()

    val browse: BrowseContent
        get() = browseContent(this)

    val inUse: ShelfModel?
        get() = shelf.find { it.id == selectedModelId }
}

sealed interface ModelsIntent : UiIntent {
    data class CatalogSourceSelected(
        val source: CatalogSource,
    ) : ModelsIntent

    data class FilterSelected(
        val filter: CatalogFilter,
    ) : ModelsIntent

    data class QueryChanged(
        val query: String,
    ) : ModelsIntent

    data object RefreshRequested : ModelsIntent

    data class DownloadClicked(
        val model: CatalogModel,
    ) : ModelsIntent

    data class CancelClicked(
        val transferId: String,
    ) : ModelsIntent

    data class TransferDismissed(
        val transferId: String,
    ) : ModelsIntent

    data class UseClicked(
        val modelId: String,
    ) : ModelsIntent

    data class DeleteRequested(
        val modelId: String,
    ) : ModelsIntent

    data object DeleteConfirmed : ModelsIntent

    data object DeleteCancelled : ModelsIntent

    data class DetailsOpened(
        val model: CatalogModel,
    ) : ModelsIntent

    data object DetailsDismissed : ModelsIntent

    data class ImportPicked(
        val uri: String,
    ) : ModelsIntent
}

sealed interface ModelsEffect : UiEffect {
    data object ShowShelf : ModelsEffect
}
