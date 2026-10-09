package com.abrarshakhi.mishti.features.models.domain.repository

import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    val shelf: Flow<List<ShelfModel>>

    val transfers: Flow<List<Transfer>>

    val storage: Flow<StorageUsage>

    val selectedModelId: Flow<String?>

    suspend fun select(modelId: String?)

    fun download(model: CatalogModel)

    fun import(uri: String)

    fun cancel(transferId: String)

    fun cancelAll()

    fun dismissTransfer(transferId: String)

    suspend fun delete(modelId: String)
}

data class StorageUsage(
    val usedBytes: Long = 0L,
    val availableBytes: Long = 0L,
)
