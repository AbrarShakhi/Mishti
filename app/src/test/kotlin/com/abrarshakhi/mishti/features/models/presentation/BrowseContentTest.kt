package com.abrarshakhi.mishti.features.models.presentation

import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.features.models.catalogModel
import com.abrarshakhi.mishti.features.models.domain.model.Catalog
import com.abrarshakhi.mishti.features.models.domain.model.CatalogGroup
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseContentTest {

    private val small = catalogModel("small", sizeBytes = 300_000_000L, tags = setOf("recommended"))
    private val coder = catalogModel("coder", sizeBytes = 1_000_000_000L, tags = setOf("coding", "recommended"))
    private val huge = catalogModel(
        "huge",
        sizeBytes = 2_500_000_000L,
        requiredRamBytes = 9_000_000_000L,
        tags = setOf("recommended"),
    )

    private fun state(
        catalog: Catalog = Catalog(
            source = CatalogSource.MistirBhandar,
            version = "1",
            models = listOf(huge, coder, small),
            recommendedIds = listOf(huge.id, small.id, coder.id),
        ),
        ram: Long = 6_000_000_000L,
        shelf: List<ShelfModel> = emptyList(),
        transfers: List<Transfer> = emptyList(),
        filter: CatalogFilter = CatalogFilter.All,
        query: String = "",
    ) = ModelsUiState(
        capability = DeviceCapability.Supported(ram),
        catalogSource = catalog.source,
        catalogs = mapOf(catalog.source to CatalogState.Ready(catalog, null, false, false)),
        shelf = shelf,
        transfers = transfers,
        filter = filter,
        query = query,
    )

    @Test
    fun `recommendations skip models too big for the phone`() {
        val content = browseContent(state())

        assertEquals(listOf(small.id, coder.id), content.recommended.map { it.model.id })
    }

    @Test
    fun `the list is sorted from smallest to largest and marks what does not fit`() {
        val items = browseContent(state()).sections.single().items

        assertEquals(listOf(small.id, coder.id, huge.id), items.map { it.model.id })
        assertEquals(MemoryFit.TooBig, items.last().fit)
    }

    @Test
    fun `filters and search narrow the list and hide recommendations while searching`() {
        assertEquals(listOf(coder.id), browseContent(state(filter = CatalogFilter.Coding)).sections.single().items.map { it.model.id })
        assertEquals(listOf(huge.id), browseContent(state(filter = CatalogFilter.Smart)).sections.single().items.map { it.model.id })

        val searched = browseContent(state(query = "cod"))
        assertEquals(listOf(coder.id), searched.sections.single().items.map { it.model.id })
        assertTrue(searched.recommended.isEmpty())
    }

    @Test
    fun `a model already downloaded from the other catalog counts as on the shelf`() {
        val shelf = ShelfModel(
            id = "pocketpal-small", name = "small", origin = ModelOrigin.PocketPal, quantization = null,
            parametersLabel = null, sizeBytes = 1, hfRepo = small.hfRepo, hfFile = small.hfFile,
            architecture = null, contextLength = null, license = null, installedAtMillis = 0,
        )

        val item = browseContent(state(shelf = listOf(shelf))).sections.single().items.first { it.model.id == small.id }

        assertTrue(item.isOnShelf)
    }

    @Test
    fun `download progress is attached to its row`() {
        val transfer = Transfer(coder.id, coder.name, TransferStatus.Downloading(5, 10))

        val item = browseContent(state(transfers = listOf(transfer))).sections.single().items.first { it.model.id == coder.id }

        assertEquals(transfer.status, item.transfer)
    }

    @Test
    fun `PocketPal tiers become sections and the phone's own tier is marked`() {
        val catalog = Catalog(
            source = CatalogSource.PocketPal,
            version = "1",
            models = listOf(small.copy(groupId = "low"), coder.copy(groupId = "mid")),
            groups = listOf(CatalogGroup("low", "Everyday phones"), CatalogGroup("mid", "Mid-range phones")),
            deviceGroupId = "mid",
        )

        val sections = browseContent(state(catalog = catalog)).sections

        assertEquals(listOf("low", "mid"), sections.map { it.id })
        assertEquals(listOf(false, true), sections.map { it.isDeviceGroup })
    }
}
