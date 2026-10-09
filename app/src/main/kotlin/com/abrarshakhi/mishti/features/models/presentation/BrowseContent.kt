package com.abrarshakhi.mishti.features.models.presentation

import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus
import com.abrarshakhi.mishti.features.models.domain.model.memoryFit

data class CatalogItem(
    val model: CatalogModel,
    val fit: MemoryFit,
    val isOnShelf: Boolean,
    val transfer: TransferStatus?,
)

data class CatalogSection(
    val id: String,
    val isDeviceGroup: Boolean,
    val items: List<CatalogItem>,
)

data class BrowseContent(
    val recommended: List<CatalogItem> = emptyList(),
    val sections: List<CatalogSection> = emptyList(),
    val filters: List<CatalogFilter> = emptyList(),
)

private const val FAST_MAX_BYTES = 600_000_000L
private const val BALANCED_MAX_BYTES = 1_500_000_000L
private const val RECOMMENDED_LIMIT = 6

const val ALL_SECTION_ID = "all"

fun CatalogModel.matches(filter: CatalogFilter): Boolean =
    when (filter) {
        CatalogFilter.All -> true
        CatalogFilter.Fast -> sizeBytes < FAST_MAX_BYTES
        CatalogFilter.Balanced -> sizeBytes in FAST_MAX_BYTES until BALANCED_MAX_BYTES
        CatalogFilter.Smart -> sizeBytes >= BALANCED_MAX_BYTES
        CatalogFilter.Coding -> "coding" in tags
    }

fun CatalogModel.matches(query: String): Boolean {
    val words =
        query
            .trim()
            .lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    if (words.isEmpty()) return true
    val haystack =
        listOfNotNull(name, publisher, description, quantization, hfRepo, parametersLabel)
            .plus(tags)
            .joinToString(" ")
            .lowercase()
    return words.all { it in haystack }
}

fun browseContent(state: ModelsUiState): BrowseContent {
    val catalog = (state.activeCatalog as? CatalogState.Ready)?.catalog ?: return BrowseContent()
    val deviceRam = state.capability.totalMemoryBytes
    val transfers = state.transfers.associateBy { it.id }

    fun item(model: CatalogModel) =
        CatalogItem(
            model = model,
            fit = memoryFit(model.requiredRamBytes, deviceRam),
            isOnShelf = state.shelf.any {
                it.id == model.id || model.isSameFileAs(
                    it.hfRepo,
                    it.hfFile
                )
            },
            transfer = transfers[model.id]?.status,
        )

    val byId = catalog.models.associateBy { it.id }
    val recommended =
        if (state.query.isBlank()) {
            catalog.recommendedIds
                .mapNotNull { byId[it] }
                .map(::item)
                .filter { it.fit != MemoryFit.TooBig }
                .take(RECOMMENDED_LIMIT)
        } else {
            emptyList()
        }

    val visible = catalog.models.filter { it.matches(state.filter) && it.matches(state.query) }
    val sections =
        if (catalog.groups.isEmpty()) {
            listOf(
                CatalogSection(
                    id = ALL_SECTION_ID,
                    isDeviceGroup = false,
                    items = visible.sortedBy { it.sizeBytes }.map(::item),
                ),
            )
        } else {
            catalog.groups.map { group ->
                CatalogSection(
                    id = group.id,
                    isDeviceGroup = group.id == catalog.deviceGroupId,
                    items = visible.filter { it.groupId == group.id }.sortedBy { it.sizeBytes }
                        .map(::item),
                )
            }
        }.filter { it.items.isNotEmpty() }

    val filters =
        CatalogFilter.entries.filter { filter ->
            filter == CatalogFilter.All || catalog.models.any { it.matches(filter) }
        }

    return BrowseContent(recommended = recommended, sections = sections, filters = filters)
}
