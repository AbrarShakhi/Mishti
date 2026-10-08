package com.abrarshakhi.mishti.features.models.data

import io.ktor.client.HttpClient
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

data class DownloadRequest(
    val id: String,
    val name: String,
    val url: String,
    val sizeBytes: Long,
    val sha256: String,
)

class ModelDownloader(
    private val client: HttpClient,
    private val storage: ModelStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun download(
        request: DownloadRequest,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): File = withContext(ioDispatcher) {
        if (!storage.hasRoomFor(request.id, request.sizeBytes)) {
            throw DownloadFailure("Not enough free space for ${request.name}.")
        }

        val partial = storage.partialFile(request.id)
        val alreadyHave = if (partial.isFile) partial.length() else 0L

        client.prepareGet(request.url) {
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
                        onProgress(DownloadProgress.Downloading(written, request.sizeBytes))
                    }
                }
            }
        }

        onProgress(DownloadProgress.Verifying)
        val actual = storage.sha256(partial)
        if (!actual.equals(request.sha256, ignoreCase = true)) {
            partial.delete()
            throw DownloadFailure("${request.name} failed verification and was discarded.")
        }

        val target = storage.modelFile(request.id)
        target.delete()
        if (!partial.renameTo(target)) {
            throw DownloadFailure("Could not save ${request.name}.")
        }
        target
    }

    private fun HttpStatusCode.isSuccessOrPartial(): Boolean =
        value in 200..299

    private companion object {
        const val DOWNLOAD_CHUNK_BYTES = 64L * 1024
    }
}
