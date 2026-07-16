package com.abrarshakhi.mishti.domain.repository

import com.abrarshakhi.mishti.domain.model.AvailableModel
import kotlinx.coroutines.flow.Flow


interface ModelRepository {
    fun getAvailableModels(): List<AvailableModel>
    fun observeDownloadedIds(): Flow<Set<String>>
    suspend fun getFilePath(modelId: String): String?
    suspend fun isDownloaded(modelId: String): Boolean
    suspend fun download(model: AvailableModel, onProgress: (Int) -> Unit): Result<String>
    suspend fun delete(modelId: String)
}
