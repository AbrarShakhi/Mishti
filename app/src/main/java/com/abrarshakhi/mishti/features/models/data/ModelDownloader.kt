package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.features.models.domain.model.LlmModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.coroutineContext

sealed interface DownloadProgress {
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : DownloadProgress
    data object Verifying : DownloadProgress
}

class DownloadFailure(message: String, cause: Throwable? = null) : Exception(message, cause)

class ModelDownloader(
    private val client: HttpClient,
    private val storage: ModelStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun download(
        model: LlmModel,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): File = withContext(ioDispatcher) {
        if (!storage.hasRoomFor(model)) {
            throw DownloadFailure("Not enough free space for ${model.name}.")
        }

        val partial = storage.partialFile(model)
        val alreadyHave = if (partial.isFile) partial.length() else 0L

        client.prepareGet(model.url) {
            if (alreadyHave > 0) header(HttpHeaders.Range, "bytes=$alreadyHave-")
        }.execute { response ->
            val resuming = response.status == HttpStatusCode.PartialContent
            if (alreadyHave > 0 && !resuming) partial.delete()
            if (!response.status.isSuccessOrPartial()) {
                throw DownloadFailure("Download failed (HTTP ${response.status.value}).")
            }

            val startAt = if (resuming) alreadyHave else 0L
            var written = startAt

            RandomAccessFile(partial, "rw").use { out ->
                out.seek(startAt)
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    coroutineContext.ensureActive()
                    val packet = channel.readBuffer(DOWNLOAD_CHUNK_BYTES)
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        out.write(bytes)
                        written += bytes.size
                        onProgress(DownloadProgress.Downloading(written, model.sizeBytes))
                    }
                }
            }
        }

        onProgress(DownloadProgress.Verifying)
        val actual = storage.sha256(partial)
        if (!actual.equals(model.sha256, ignoreCase = true)) {
            partial.delete()
            throw DownloadFailure("${model.name} failed verification and was discarded.")
        }

        val target = storage.modelFile(model)
        target.delete()
        if (!partial.renameTo(target)) {
            throw DownloadFailure("Could not save ${model.name}.")
        }
        target
    }

    private fun HttpStatusCode.isSuccessOrPartial(): Boolean =
        value in 200..299

    private companion object {
        const val DOWNLOAD_CHUNK_BYTES = 64L * 1024
    }
}
