package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.features.models.domain.model.ModelCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesSelectedModelSource(
    private val preferences: AppPreferences,
    private val storage: ModelStorage,
) : SelectedModelSource {

    override fun selected(): Flow<ModelHandle?> = preferences.selectedModelId.map { id ->
        val model = id?.let { ModelCatalog.byId(it) } ?: return@map null
        if (!storage.isDownloaded(model)) return@map null
        ModelHandle(
            id = model.id,
            name = model.name,
            path = storage.modelFile(model).absolutePath,
        )
    }
}
