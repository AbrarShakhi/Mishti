package com.abrarshakhi.mishti.core.session.data

import com.abrarshakhi.mishti.core.domain.preferences.AppPreferences
import com.abrarshakhi.mishti.core.session.domain.ModelSelector
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.core.storage.models.InstalledModelDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DefaultModelSelector(private val dao: InstalledModelDao, private val preferences: AppPreferences) :
    ModelSelector {
    override val selectedModelId: Flow<String?> = preferences.selectedModelId

    override val availableModels: Flow<List<SelectableModel>> = combine(
        dao.observeAll(),
        preferences.selectedModelId,
    ) { rows, selectedId ->
        rows.map { entity ->
            SelectableModel(
                id = entity.id,
                name = entity.name,
                parametersLabel = entity.parametersLabel,
                quantization = entity.quantization,
                isSelected = entity.id == selectedId,
            )
        }
    }

    override suspend fun selectModel(modelId: String?) {
        preferences.setSelectedModelId(modelId)
    }
}
