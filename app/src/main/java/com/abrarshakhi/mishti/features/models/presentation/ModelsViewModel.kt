package com.abrarshakhi.mishti.features.models.presentation

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ModelsViewModel(
    private val repository: ModelRepository,
    private val snackbar: SnackbarDispatcher,
    capabilityProvider: DeviceCapabilityProvider,
) : MviViewModel<ModelsUiState, ModelsIntent, ModelsEffect>(ModelsUiState()) {

    private val capability = capabilityProvider.capability()

    init {
        updateState { copy(capability = capability) }
        viewModelScope.launch {
            combine(
                repository.entries,
                repository.storage,
                repository.selectedModelId,
            ) { entries, storage, selected ->
                Triple(entries, storage, selected)
            }.collect { (entries, storage, selected) ->
                updateState {
                    copy(entries = entries, storage = storage, selectedModelId = selected)
                }
            }
        }
    }

    override fun handleIntent(intent: ModelsIntent) {
        when (intent) {
            is ModelsIntent.DownloadClicked -> onDownload(intent.modelId)
            is ModelsIntent.CancelClicked -> repository.cancel(intent.modelId)
            is ModelsIntent.SelectClicked -> viewModelScope.launch {
                repository.select(intent.modelId)
            }

            is ModelsIntent.DeleteRequested -> updateState {
                copy(deleting = entries.find { it.model.id == intent.modelId })
            }

            ModelsIntent.DeleteConfirmed -> onDeleteConfirmed()
            ModelsIntent.DeleteCancelled -> updateState { copy(deleting = null) }
        }
    }

    private fun onDownload(modelId: String) {
        val entry = currentState.entries.find { it.model.id == modelId } ?: return
        val total = capability.totalMemoryBytes

        if (capability is DeviceCapability.UnsupportedLowMemory) {
            snackbar.showError("This device does not have enough memory to run a model.")
            return
        }
        if (total != null && total < entry.model.minRamBytes) {
            snackbar.showError("${entry.model.name} needs more memory than this device has.")
            return
        }
        repository.download(modelId)
    }

    private fun onDeleteConfirmed() {
        val target = currentState.deleting ?: return
        updateState { copy(deleting = null) }
        viewModelScope.launch {
            runCatching { repository.delete(target.model.id) }.onSuccess { snackbar.show("${target.model.name} deleted.") }
                .onFailure { snackbar.showError("Could not delete ${target.model.name}.") }
        }
    }
}
