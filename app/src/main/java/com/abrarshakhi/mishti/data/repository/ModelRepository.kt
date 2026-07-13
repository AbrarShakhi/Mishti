package com.abrarshakhi.mishti.data.repository

import android.content.Context
import com.abrarshakhi.mishti.data.db.ModelDao
import com.abrarshakhi.mishti.data.db.ModelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


data class AvailableModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String
)

val AVAILABLE_MODELS = listOf(
    AvailableModel(
        id = "tinyllama-1.1b-q4",
        name = "TinyLlama 1.1B (Q4_K_M)",
        description = "Super fast, ~670 MB. Great for testing on any device.",
        sizeBytes = 669_000_000L,
        downloadUrl = "https:
    ),
    AvailableModel(
        id = "phi3-mini-q4",
        name = "Phi-3 Mini 3.8B (Q4_K_M)",
        description = "Smart and small, ~2.2 GB. Needs 4 GB+ RAM.",
        sizeBytes = 2_200_000_000L,
        downloadUrl = "https:
    ),
    AvailableModel(
        id = "gemma3-1b-q8",
        name = "Gemma 3 1B (Q8)",
        description = "Google's compact model, ~1 GB. Good quality for its size.",
        sizeBytes = 1_000_000_000L,
        downloadUrl = "https:
    )
)


sealed class DownloadState {
    data class Progress(val percent: Int) : DownloadState()
    data class Success(val filePath: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}


class ModelRepository(
    private val context: Context,
    private val dao: ModelDao,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    /** All models the user has already downloaded — updates automatically. */
    val downloadedModels: Flow<List<ModelEntity>> = dao.observeAll()

    /** The hard-coded list of models available to download. */
    fun getAvailableModels(): List<AvailableModel> = AVAILABLE_MODELS

    /** True if a given model ID is already on disk. */
    suspend fun isDownloaded(modelId: String): Boolean =
        dao.getById(modelId) != null

    /**
     * Download a model file to internal storage and record it in Room.
     *
     * We use a [kotlinx.coroutines.flow.flow] so the ViewModel can observe
     * progress and update a progress bar without us needing callbacks.
     *
     * The file goes to:  <filesDir>/models/<modelId>.gguf
     */
    suspend fun download(
        model: AvailableModel,
        onProgress: (Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val modelsDir = File(context.filesDir, "models").also { it.mkdirs() }
        val destFile = File(modelsDir, "${model.id}.gguf")


        if (destFile.exists()) destFile.delete()

        try {
            val request = Request.Builder().url(model.downloadUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP ${response.code}: ${response.message}")
                )
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val contentLength = body.contentLength()
            var bytesRead = 0L

            destFile.outputStream().use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                        bytesRead += read

                        if (contentLength > 0) {
                            val percent = (bytesRead * 100 / contentLength).toInt()
                            onProgress(percent)
                        }
                    }
                }
            }


            dao.upsert(
                ModelEntity(
                    id = model.id,
                    name = model.name,
                    filePath = destFile.absolutePath,
                    sizeBytes = destFile.length()
                )
            )

            Result.success(destFile.absolutePath)

        } catch (e: Exception) {

            if (destFile.exists()) destFile.delete()
            Result.failure(e)
        }
    }

    /**
     * Delete a model's file from disk and remove it from Room.
     */
    suspend fun delete(model: ModelEntity) = withContext(Dispatchers.IO) {
        File(model.filePath).delete()
        dao.delete(model)
    }

    /**
     * Return the absolute file path for a downloaded model, or null.
     */
    suspend fun getFilePath(modelId: String): String? =
        dao.getById(modelId)?.filePath
}