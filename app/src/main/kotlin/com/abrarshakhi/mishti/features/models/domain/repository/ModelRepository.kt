package com.abrarshakhi.mishti.features.models.domain.repository

import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import kotlinx.coroutines.flow.Flow


interface ModelRepository {

    val entries: Flow<List<ModelEntry>>

    val storage: Flow<StorageUsage>

    val selectedModelId: Flow<String?>

    suspend fun select(modelId: String?)

    fun download(modelId: String)

    fun cancel(modelId: String)

    fun cancelAll()

    suspend fun delete(modelId: String)
}

data class StorageUsage(
    val usedBytes: Long = 0L,
    val availableBytes: Long = 0L,
)
