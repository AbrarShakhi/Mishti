package com.abrarshakhi.mishti.models.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.device.DeviceCapability
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState
import com.abrarshakhi.mishti.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.CatalogState
import com.abrarshakhi.mishti.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.models.domain.model.Transfer
import com.abrarshakhi.mishti.models.domain.repository.StorageUsage

sealed interface ModelsIntent : UiIntent {
    data class CatalogSourceSelected(val source: CatalogSource) : ModelsIntent

    data class FilterSelected(val filter: CatalogFilter) : ModelsIntent

    data class QueryChanged(val query: String) : ModelsIntent

    data object RefreshRequested : ModelsIntent

    data class DownloadClicked(val model: CatalogModel) : ModelsIntent

    data class CancelClicked(val transferId: String) : ModelsIntent

    data class TransferDismissed(val transferId: String) : ModelsIntent

    data class UseClicked(val modelId: String) : ModelsIntent

    data class DeleteRequested(val modelId: String) : ModelsIntent

    data object DeleteConfirmed : ModelsIntent

    data object DeleteCancelled : ModelsIntent

    data class DetailsOpened(val model: CatalogModel) : ModelsIntent

    data object DetailsDismissed : ModelsIntent

    data class ImportPicked(val uri: String) : ModelsIntent
}
