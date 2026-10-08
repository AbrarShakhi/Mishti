package com.abrarshakhi.mishti.features.models.presentation

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.model.memoryFit
import com.abrarshakhi.mishti.features.models.domain.repository.CatalogRepository
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

class ModelsViewModel(
    private val repository: ModelRepository,
    private val catalogs: CatalogRepository,
    private val snackbar: SnackbarDispatcher,
    capabilityProvider: DeviceCapabilityProvider,
) : MviViewModel<ModelsUiState, ModelsIntent, ModelsEffect>(ModelsUiState()) {

    private val deviceCapability = capabilityProvider.capability()

    init {
        updateState { copy(capability = deviceCapability) }
        viewModelScope.launch {
            combine(
                repository.shelf,
                repository.transfers,
                repository.storage,
                repository.selectedModelId,
            ) { shelf, transfers, storage, selected ->
                ShelfSnapshot(shelf, transfers, storage, selected)
            }.collect { snapshot ->
                updateState {
                    copy(
                        isLoading = false,
                        shelf = snapshot.shelf,
                        transfers = snapshot.transfers,
                        storage = snapshot.storage,
                        selectedModelId = snapshot.selected,
                    )
                }
            }
        }
        CatalogSource.entries.forEach { source ->
            viewModelScope.launch {
                catalogs.state(source).collect { catalog ->
                    updateState { copy(catalogs = catalogs + (source to catalog)) }
                }
            }
        }
        refresh(CatalogSource.MistirBhandar, force = false)
    }

    override fun handleIntent(intent: ModelsIntent) {
        when (intent) {
            is ModelsIntent.CatalogSourceSelected -> {
                updateState { copy(catalogSource = intent.source, filter = CatalogFilter.All) }
                refresh(intent.source, force = false)
            }
            is ModelsIntent.FilterSelected -> updateState { copy(filter = intent.filter) }
            is ModelsIntent.QueryChanged -> updateState { copy(query = intent.query) }
            ModelsIntent.RefreshRequested -> refresh(currentState.catalogSource, force = true)
            is ModelsIntent.DownloadClicked -> onDownload(intent.model)
            is ModelsIntent.CancelClicked -> repository.cancel(intent.transferId)
            is ModelsIntent.TransferDismissed -> repository.dismissTransfer(intent.transferId)
            is ModelsIntent.UseClicked -> onUse(intent.modelId)
            is ModelsIntent.DeleteRequested -> updateState {
                copy(deleting = shelf.find { it.id == intent.modelId })
            }
            ModelsIntent.DeleteConfirmed -> onDeleteConfirmed()
            ModelsIntent.DeleteCancelled -> updateState { copy(deleting = null) }
            is ModelsIntent.DetailsOpened -> updateState { copy(details = intent.model) }
            ModelsIntent.DetailsDismissed -> updateState { copy(details = null) }
            is ModelsIntent.ImportPicked -> {
                repository.import(intent.uri)
                emitEffect(ModelsEffect.ShowShelf)
            }
        }
    }

    private fun refresh(source: CatalogSource, force: Boolean) {
        viewModelScope.launch { catalogs.refresh(source, force) }
    }

    private fun onDownload(model: CatalogModel) {
        if (deviceCapability is DeviceCapability.UnsupportedLowMemory) {
            snackbar.showError("This phone does not have enough memory to run a model.")
            return
        }
        if (memoryFit(model.requiredRamBytes, deviceCapability.totalMemoryBytes) == MemoryFit.TooBig) {
            snackbar.showError(
                "${model.name} needs about ${gigabytes(model.requiredRamBytes)} of memory.",
            )
            return
        }
        updateState { copy(details = null) }
        repository.download(model)
    }

    private fun onUse(modelId: String) {
        viewModelScope.launch {
            try {
                repository.select(modelId)
                currentState.shelf.find { it.id == modelId }?.let {
                    snackbar.show("Chatting with ${it.name} now.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackbar.showError("Could not switch models.")
            }
        }
    }

    private fun onDeleteConfirmed() {
        val target = currentState.deleting ?: return
        updateState { copy(deleting = null) }
        viewModelScope.launch {
            try {
                repository.delete(target.id)
                snackbar.show("${target.name} deleted.")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackbar.showError("Could not delete ${target.name}.")
            }
        }
    }

    private data class ShelfSnapshot(
        val shelf: List<ShelfModel>,
        val transfers: List<Transfer>,
        val storage: StorageUsage,
        val selected: String?,
    )
}

fun gigabytes(bytes: Long): String = String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
