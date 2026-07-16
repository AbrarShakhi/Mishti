package com.abrarshakhi.mishti.data.repository

import android.content.Context
import com.abrarshakhi.mishti.data.db.ModelDao
import com.abrarshakhi.mishti.data.entity.ModelEntity
import com.abrarshakhi.mishti.domain.model.AvailableModel
import com.abrarshakhi.mishti.domain.repository.ModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

private val CATALOG = listOf(
    AvailableModel(
        id = "tinyllama-1.1b-q4",
        name = "TinyLlama 1.1B (Q4_K_M)",
        description = "Super fast, ~670 MB. Great for testing on any device.",
        sizeBytes = 669_000_000L,
        downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
    ),
    AvailableModel(
        id = "phi3-mini-q4",
        name = "Phi-3 Mini 3.8B (Q4_K_M)",
        description = "Smart and small, ~2.2 GB. Needs 4 GB+ RAM.",
        sizeBytes = 2_200_000_000L,
        downloadUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
    ),
    AvailableModel(
        id = "gemma3-1b-q8",
        name = "Gemma 3 1B (Q8)",
        description = "Google's compact model, ~1 GB. Good quality for its size.",
        sizeBytes = 1_000_000_000L,
        downloadUrl = "https://huggingface.co/lmstudio-community/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-Q8_0.gguf",
    ),
)

class ModelRepositoryImpl(
    private val context: Context,
    private val dao: ModelDao,
    private val httpClient: OkHttpClient = OkHttpClient(),
) : ModelRepository {

    override fun getAvailableModels(): List<AvailableModel> = CATALOG

    override fun observeDownloadedIds(): Flow<Set<String>> =
        dao.observeAll().map { entities -> entities.map { it.id }.toSet() }

    override suspend fun getFilePath(modelId: String): String? =
        dao.getById(modelId)?.filePath

    override suspend fun isDownloaded(modelId: String): Boolean =
        dao.getById(modelId) != null

    override suspend fun download(
        model: AvailableModel,
        onProgress: (Int) -> Unit,
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

            val body = response.body
                ?: return@withContext Result.failure(Exception("Empty response body"))

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
                            onProgress((bytesRead * 100 / contentLength).toInt())
                        }
                    }
                }
            }

            dao.upsert(
                ModelEntity(
                    id = model.id,
                    name = model.name,
                    filePath = destFile.absolutePath,
                    sizeBytes = destFile.length(),
                )
            )

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            if (destFile.exists()) destFile.delete()
            Result.failure(e)
        }
    }

    override suspend fun delete(modelId: String) = withContext(Dispatchers.IO) {
        val entity = dao.getById(modelId) ?: return@withContext
        File(entity.filePath).delete()
        dao.delete(entity)
    }
}
