package com.abrarshakhi.mishti.features.models

import com.abrarshakhi.mishti.common.MainDispatcherRule
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.models.domain.model.ModelCatalog
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.model.ModelStatus
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import com.abrarshakhi.mishti.features.models.presentation.ModelsIntent
import com.abrarshakhi.mishti.features.models.presentation.ModelsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeModelRepository : ModelRepository {
    private val _entries = MutableStateFlow(
        ModelCatalog.models.map { ModelEntry(it, ModelStatus.NotDownloaded) }
    )
    override val entries: Flow<List<ModelEntry>> = _entries
    override val storage: Flow<StorageUsage> = MutableStateFlow(StorageUsage())

    private val _selected = MutableStateFlow<String?>(null)
    override val selectedModelId: Flow<String?> = _selected

    var downloadedIds: Set<String> = emptySet()

    override suspend fun select(modelId: String?) {
        if (modelId == null || modelId in downloadedIds) _selected.value = modelId
    }

    val downloaded = mutableListOf<String>()
    val deleted = mutableListOf<String>()
    var cancelled: String? = null

    override fun download(modelId: String) { downloaded += modelId }
    override fun cancel(modelId: String) { cancelled = modelId }
    override fun cancelAll() { cancelled = "all" }
    override suspend fun delete(modelId: String) { deleted += modelId }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ModelsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val smallest = ModelCatalog.models.first()
    private val largest = ModelCatalog.models.last()

    private fun viewModel(
        repository: FakeModelRepository = FakeModelRepository(),
        capability: DeviceCapability = DeviceCapability.Supported(8_000_000_000L),
    ) = ModelsViewModel(
        repository = repository,
        snackbar = SnackbarDispatcher(),
        capabilityProvider = DeviceCapabilityProvider { capability },
    )

    @Test
    fun `a capable device can download`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DownloadClicked(smallest.id))

        assertEquals(listOf(smallest.id), repository.downloaded)
    }

    @Test
    fun `a low memory device is refused before any bytes are spent`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(
            repository,
            capability = DeviceCapability.UnsupportedLowMemory(totalMemoryBytes = 2_000_000_000L),
        )
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DownloadClicked(smallest.id))

        assertTrue(repository.downloaded.isEmpty())
    }

    @Test
    fun `gating is per model, not global`() = runTest {
        val repository = FakeModelRepository()
        // Enough for the small model, short of the 1B one's floor.
        val vm = viewModel(repository, capability = DeviceCapability.Supported(3_500_000_000L))
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DownloadClicked(largest.id))
        assertTrue("large model should be refused", repository.downloaded.isEmpty())

        vm.onIntent(ModelsIntent.DownloadClicked(smallest.id))
        assertEquals(listOf(smallest.id), repository.downloaded)
    }

    @Test
    fun `delete asks for confirmation before removing the file`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DeleteRequested(smallest.id))
        advanceUntilIdle()
        assertEquals(smallest.id, vm.state.value.deleting?.model?.id)
        assertTrue(repository.deleted.isEmpty())

        vm.onIntent(ModelsIntent.DeleteConfirmed)
        advanceUntilIdle()
        assertEquals(listOf(smallest.id), repository.deleted)
    }

    @Test
    fun `cancelling the confirmation keeps the file`() = runTest {
        val repository = FakeModelRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.DeleteRequested(smallest.id))
        vm.onIntent(ModelsIntent.DeleteCancelled)
        advanceUntilIdle()

        assertTrue(repository.deleted.isEmpty())
    }

    @Test
    fun `selecting a downloaded model records it`() = runTest {
        val repository = FakeModelRepository().apply { downloadedIds = setOf(smallest.id) }
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.SelectClicked(smallest.id))
        advanceUntilIdle()

        assertEquals(smallest.id, vm.state.value.selectedModelId)
    }

    @Test
    fun `selecting a model that is not downloaded is ignored`() = runTest {
        // Otherwise the engine would be pointed at a file that does not exist and the user
        // would see a selection that silently does nothing.
        val repository = FakeModelRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.onIntent(ModelsIntent.SelectClicked(smallest.id))
        advanceUntilIdle()

        assertEquals(null, vm.state.value.selectedModelId)
    }

    @Test
    fun `every catalogue entry has a plausible url, size and checksum`() {
        ModelCatalog.models.forEach { model ->
            assertTrue(model.url.startsWith("https://"))
            assertTrue("${model.id} size", model.sizeBytes > 1_000_000L)
            assertEquals("${model.id} sha256 length", 64, model.sha256.length)
            assertTrue("${model.id} sha256 hex", model.sha256.all { it in "0123456789abcdef" })
        }
        assertEquals(
            "ids must be unique",
            ModelCatalog.models.size,
            ModelCatalog.models.map { it.id }.toSet().size,
        )
    }
}
