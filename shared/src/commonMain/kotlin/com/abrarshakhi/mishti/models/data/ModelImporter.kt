package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.data.gguf.GgufInfo
import com.abrarshakhi.mishti.models.data.gguf.GgufReader
import com.abrarshakhi.mishti.models.data.gguf.NotGgufException
import com.abrarshakhi.mishti.models.domain.model.TransferError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okio.EOFException
import okio.buffer
import okio.use

data class ImportedFile(val displayName: String, val sizeBytes: Long, val info: GgufInfo)

class ModelImporter(
    private val source: ModelImportSource,
    private val storage: ModelStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun displayName(uri: String): String = withContext(ioDispatcher) { source.metadata(uri).name }

    suspend fun import(id: String, uri: String, onProgress: suspend (Long, Long) -> Unit): ImportedFile =
        withContext(ioDispatcher) {
            val metadata = source.metadata(uri)
            if (metadata.sizeBytes > 0 && !storage.hasRoomFor(id, metadata.sizeBytes)) {
                throw DownloadFailure(TransferError.NotEnoughSpace)
            }
            val partial = storage.partialFile(id)
            try {
                var copied = 0L
                source.open(uri).buffer().use { input ->
                    storage.fileSystem.sink(partial).buffer().use { output ->
                        val buffer = okio.Buffer()
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer, 256 * 1024L)
                            if (count == -1L) break
                            output.write(buffer, count)
                            copied += count
                            onProgress(copied, metadata.sizeBytes)
                        }
                    }
                }
                val info = try {
                    storage.fileSystem.source(partial).buffer().use { GgufReader.read(it) }
                } catch (_: EOFException) {
                    throw DownloadFailure(TransferError.NotGguf)
                } catch (_: NotGgufException) {
                    throw DownloadFailure(TransferError.NotGguf)
                }
                storage.commit(id)
                ImportedFile(metadata.name, copied, info)
            } catch (error: Throwable) {
                storage.fileSystem.delete(partial, mustExist = false)
                throw error
            }
        }
}
