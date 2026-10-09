package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.features.models.data.local.InstalledModelDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

class PreferencesSelectedModelSource(
    private val preferences: AppPreferences,
    private val dao: InstalledModelDao,
    private val storage: ModelStorage,
) : SelectedModelSource {
    override fun selected(): Flow<ModelHandle?> = combine(preferences.selectedModelId, dao.observeAll()) {
            id,
            rows,
        ->
        val model = id?.let { wanted -> rows.find { it.id == wanted } } ?: return@combine null
        if (!storage.isPresent(model.id)) return@combine null
        ModelHandle(
            id = model.id,
            name = model.name,
            path = storage.modelFile(model.id).absolutePath,
        )
    }.distinctUntilChanged().flowOn(Dispatchers.IO)
}
