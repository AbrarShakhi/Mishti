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
import com.abrarshakhi.mishti.models.ui.utils.BrowseContent
import com.abrarshakhi.mishti.models.ui.utils.browseContent

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
