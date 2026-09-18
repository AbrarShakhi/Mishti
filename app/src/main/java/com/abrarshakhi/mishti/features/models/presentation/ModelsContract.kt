package com.abrarshakhi.mishti.features.models.presentation

import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.mvi.UiEffect
import com.abrarshakhi.mishti.common.mvi.UiIntent
import com.abrarshakhi.mishti.common.mvi.UiState
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage

data class ModelsUiState(
    val entries: List<ModelEntry> = emptyList(),
    val storage: StorageUsage = StorageUsage(),
    val capability: DeviceCapability = DeviceCapability.Unknown,
    val selectedModelId: String? = null,
    val deleting: ModelEntry? = null,
) : UiState

sealed interface ModelsIntent : UiIntent {
    data class DownloadClicked(val modelId: String) : ModelsIntent

    data class SelectClicked(val modelId: String) : ModelsIntent

    data class CancelClicked(val modelId: String) : ModelsIntent

    data class DeleteRequested(val modelId: String) : ModelsIntent

    data object DeleteConfirmed : ModelsIntent

    data object DeleteCancelled : ModelsIntent
}

sealed interface ModelsEffect : UiEffect
