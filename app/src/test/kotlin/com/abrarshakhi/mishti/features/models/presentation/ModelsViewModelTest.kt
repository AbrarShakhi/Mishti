package com.abrarshakhi.mishti.features.models.presentation

import com.abrarshakhi.mishti.common.MainDispatcherRule
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.models.catalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.repository.CatalogRepository
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeModelRepository : ModelRepository {
    override val shelf = MutableStateFlow<List<ShelfModel>>(emptyList())
    override val transfers = MutableStateFlow<List<Transfer>>(emptyList())
    override val storage: Flow<StorageUsage> = MutableStateFlow(StorageUsage())
    override val selectedModelId = MutableStateFlow<String?>(null)

    val downloaded = mutableListOf<String>()
    val imported = mutableListOf<String>()
    val deleted = mutableListOf<String>()

    override suspend fun select(modelId: String?) { selectedModelId.value = modelId }
    override fun download(model: CatalogModel) { downloaded += model.id }
    override fun import(uri: String) { imported += uri }
    override fun cancel(transferId: String) = Unit
    override fun cancelAll() = Unit
    override fun dismissTransfer(transferId: String) = Unit
    override suspend fun delete(modelId: String) { deleted += modelId }
}

private class FakeCatalogRepository : CatalogRepository {
    val states = CatalogSource.entries.associateWith { MutableStateFlow<CatalogState>(CatalogState.Loading(false)) }
    val refreshes = mutableListOf<Pair<CatalogSource, Boolean>>()
    override fun state(source: CatalogSource) = states.getValue(source)
    override suspend fun refresh(source: CatalogSource, force: Boolean) { refreshes += source to force }
}

private fun shelfModel(id: String) = ShelfModel(
    id = id, name = "Model $id", origin = ModelOrigin.MistirBhandar, quantization = null,
    parametersLabel = null, sizeBytes = 1, hfRepo = null, hfFile = null, architecture = null,
    contextLength = null, license = null, installedAtMillis = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ModelsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val small = catalogModel("small", requiredRamBytes = 3_200_000_000L)
    private val large = catalogModel("large", requiredRamBytes = 8_000_000_000L)

    private fun viewModel(
        repository: FakeModelRepository = FakeModelRepository(),
        catalogs: FakeCatalogRepository = FakeCatalogRepository(),
        capability: DeviceCapability = DeviceCapability.Supported(6_000_000_000L),
    ) = ModelsViewModel(
        repository = repository,
        catalogs = catalogs,
        snackbar = SnackbarDispatcher(),
        capabilityProvider = DeviceCapabilityProvider { capability },
    )

    @Test
    fun `opening the shop refreshes only Mistir Bhandar`() = runTest {
        val catalogs = FakeCatalogRepository()
        viewModel(catalogs = catalogs)
        advanceUntilIdle()

        assertEquals(listOf(CatalogSource.MistirBhandar to false), catalogs.refreshes)
    }

    @Test
    fun `switching to PocketPal fetches its list and resets the filter`() = runTest {
        val catalogs = FakeCatalogRepository()
        val vm = viewModel(catalogs = catalogs)
        vm.onIntent(ModelsIntent.FilterSelected(CatalogFilter.Coding))

        vm.onIntent(ModelsIntent.CatalogSourceSelected(CatalogSource.PocketPal))
        advanceUntilIdle()

        assertEquals(CatalogSource.PocketPal to false, catalogs.refreshes.last())
        assertEquals(CatalogFilter.All, vm.state.value.filter)
    }

    @Test
    fun `pulling to refresh forces a fetch of the open catalog`() = runTest {
        val catalogs = FakeCatalogRepository()
        val vm = viewModel(catalogs = catalogs)

        vm.onIntent(ModelsIntent.RefreshRequested)
        advanceUntilIdle()

        assertEquals(CatalogSource.MistirBhandar to true, catalogs.refreshes.last())
    }

    @Test
    fun `the phone's memory reaches the screen state`() = runTest {
        val vm = viewModel(capability = DeviceCapability.Supported(6_000_000_000L))

        assertEquals(6_000_000_000L, vm.state.value.capability.totalMemoryBytes)
    }

    @Test
    fun `a model that fits is downloaded and its details close`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)
        vm.onIntent(ModelsIntent.DetailsOpened(small))

        vm.onIntent(ModelsIntent.DownloadClicked(small))

        assertEquals(listOf(small.id), repository.downloaded)
        assertEquals(null, vm.state.value.details)
    }

    @Test
    fun `a model too big for the phone is refused before any bytes are spent`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)

        vm.onIntent(ModelsIntent.DownloadClicked(large))

        assertTrue(repository.downloaded.isEmpty())
    }

    @Test
    fun `a phone below the memory floor cannot download anything`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository, capability = DeviceCapability.UnsupportedLowMemory(2_000_000_000L))

        vm.onIntent(ModelsIntent.DownloadClicked(small))

        assertTrue(repository.downloaded.isEmpty())
    }

    @Test
    fun `importing a file starts the import and shows the shelf`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)

        vm.onIntent(ModelsIntent.ImportPicked("content://downloads/model.gguf"))

        assertEquals(listOf("content://downloads/model.gguf"), repository.imported)
        assertEquals(ModelsEffect.ShowShelf, vm.effects.first())
    }

    @Test
    fun `using a shelf model selects it`() = runTest {
        val repository = FakeModelRepository().apply { shelf.value = listOf(shelfModel("a")) }
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.UseClicked("a"))
        advanceUntilIdle()

        assertEquals("a", vm.state.value.selectedModelId)
        assertEquals("a", vm.state.value.inUse?.id)
    }

    @Test
    fun `delete asks for confirmation before removing the file`() = runTest {
        val repository = FakeModelRepository().apply { shelf.value = listOf(shelfModel("a")) }
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DeleteRequested("a"))
        assertEquals("a", vm.state.value.deleting?.id)
        assertTrue(repository.deleted.isEmpty())

        vm.onIntent(ModelsIntent.DeleteConfirmed)
        advanceUntilIdle()
        assertEquals(listOf("a"), repository.deleted)
    }

    @Test
    fun `cancelling the confirmation keeps the file`() = runTest {
        val repository = FakeModelRepository().apply { shelf.value = listOf(shelfModel("a")) }
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DeleteRequested("a"))
        vm.onIntent(ModelsIntent.DeleteCancelled)
        advanceUntilIdle()

        assertTrue(repository.deleted.isEmpty())
    }
}
