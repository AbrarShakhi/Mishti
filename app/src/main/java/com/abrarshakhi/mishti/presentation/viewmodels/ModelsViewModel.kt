package com.abrarshakhi.mishti.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.data.db.ModelEntity
import com.abrarshakhi.mishti.data.repository.AvailableModel
import com.abrarshakhi.mishti.data.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModelsUiState(
    // map of modelId → download progress (0-100), only present while downloading
    val downloadProgress: Map<String, Int> = emptyMap(),
    val downloadError: Map<String, String> = emptyMap()
)

class ModelsViewModel(private val repository: ModelRepository) : ViewModel() {

    // Already-downloaded models, live from Room
    val downloadedModels: StateFlow<List<ModelEntity>> = repository.downloadedModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // The static catalogue
    val availableModels: List<AvailableModel> = repository.getAvailableModels()

    private val _uiState = MutableStateFlow(ModelsUiState())
    val uiState: StateFlow<ModelsUiState> = _uiState.asStateFlow()

    fun downloadModel(model: AvailableModel) {
        viewModelScope.launch {
            // Show 0% progress immediately
            _uiState.update { it.copy(downloadProgress = it.downloadProgress + (model.id to 0)) }

            val result = repository.download(model) { percent ->
                _uiState.update {
                    it.copy(downloadProgress = it.downloadProgress + (model.id to percent))
                }
            }

            result.fold(
                onSuccess = {
                    // Remove progress entry — the Room flow will update the downloaded list
                    _uiState.update {
                        it.copy(downloadProgress = it.downloadProgress - model.id)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = it.downloadProgress - model.id,
                            downloadError    = it.downloadError + (model.id to (error.message ?: "Unknown error"))
                        )
                    }
                }
            )
        }
    }

    fun deleteModel(model: ModelEntity) {
        viewModelScope.launch { repository.delete(model) }
    }

    fun dismissError(modelId: String) {
        _uiState.update { it.copy(downloadError = it.downloadError - modelId) }
    }

    class Factory(private val repository: ModelRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ModelsViewModel(repository) as T
    }
}