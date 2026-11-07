package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.domain.model.TransferError
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okio.Path
import okio.buffer
import okio.use

sealed interface DownloadProgress {
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : DownloadProgress
    data object Verifying : DownloadProgress
}
class DownloadFailure(val error: TransferError, cause: Throwable? = null) : Exception(error.name, cause)
data class DownloadRequest(val id: String, val name: String, val url: String, val sizeBytes: Long, val sha256: String)

class ModelDownloader(
    private val client: HttpClient,
    private val storage: ModelStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun download(request: DownloadRequest, onProgress: suspend (DownloadProgress) -> Unit): Path =
        withContext(ioDispatcher) {
            if (request.sizeBytes <= 0 || !request.sha256.matches(Regex("[a-fA-F0-9]{64}"))) {
                throw DownloadFailure(TransferError.VerificationFailed)
            }
            val partial = storage.partialFile(request.id)
            if (storage.partialBytes(request.id) > request.sizeBytes) storage.fileSystem.delete(partial)
            if (!storage.hasRoomFor(request.id, request.sizeBytes)) {
                throw DownloadFailure(TransferError.NotEnoughSpace)
            }
            val alreadyHave = storage.partialBytes(request.id)
            if (alreadyHave < request.sizeBytes) {
                client.prepareGet(request.url) {
                    if (alreadyHave > 0) header(HttpHeaders.Range, "bytes=$alreadyHave-")
                    header(HttpHeaders.AcceptEncoding, "identity")
                }.execute { response ->
                    val resuming = response.status == HttpStatusCode.PartialContent
                    if (response.status != HttpStatusCode.OK && !resuming) {
                        throw DownloadFailure(TransferError.ServerError)
                    }
                    if (resuming && response.headers[HttpHeaders.ContentRange]
                            ?.startsWith("bytes $alreadyHave-") != true
                    ) {
                        throw DownloadFailure(TransferError.ServerError)
                    }
                    var written = if (resuming) alreadyHave else 0L
                    val sink = if (resuming) {
                        storage.fileSystem.appendingSink(
                            partial,
                        )
                    } else {
                        storage.fileSystem.sink(partial)
                    }
                    sink.buffer().use { output ->
                        val channel = response.bodyAsChannel()
                        val bytes = ByteArray(128 * 1024)
                        var lastReported = written
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = channel.readAvailable(bytes, 0, bytes.size)
                            if (count == -1) break
                            if (count == 0) continue
                            written += count
                            if (written > request.sizeBytes) throw DownloadFailure(TransferError.VerificationFailed)
                            output.write(bytes, 0, count)
                            if (written - lastReported >= 1024 * 1024) {
                                onProgress(DownloadProgress.Downloading(written, request.sizeBytes))
                                lastReported = written
                            }
                        }
                    }
                    onProgress(DownloadProgress.Downloading(written, request.sizeBytes))
                }
            }
            onProgress(DownloadProgress.Verifying)
            if (storage.partialBytes(request.id) != request.sizeBytes ||
                !storage.sha256(partial).equals(request.sha256, ignoreCase = true)
            ) {
                storage.fileSystem.delete(partial, mustExist = false)
                throw DownloadFailure(TransferError.VerificationFailed)
            }
            storage.commit(request.id)
            storage.modelFile(request.id)
        }
}
