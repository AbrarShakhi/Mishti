package com.abrarshakhi.mishti.core.session.domain

import com.abrarshakhi.mishti.core.session.model.SelectableModel
import kotlinx.coroutines.flow.Flow

interface ModelSelector {
    val availableModels: Flow<List<SelectableModel>>
    val selectedModelId: Flow<String?>
    suspend fun selectModel(modelId: String?)
}
