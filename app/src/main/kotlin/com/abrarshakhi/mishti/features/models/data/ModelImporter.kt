package com.abrarshakhi.mishti.features.models.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.abrarshakhi.mishti.features.models.data.gguf.GgufInfo
import com.abrarshakhi.mishti.features.models.data.gguf.GgufReader
import com.abrarshakhi.mishti.features.models.data.gguf.NotGgufException
import com.abrarshakhi.mishti.features.models.domain.model.TransferError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.EOFException

data class ImportedFile(
    val displayName: String,
    val sizeBytes: Long,
    val info: GgufInfo,
)

class ModelImporter(
    private val context: Context,
    private val storage: ModelStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun displayName(uri: String): String = withContext(ioDispatcher) {
        query(uri.toUri()).first
    }

    suspend fun import(
        id: String,
        uri: String,
        onProgress: suspend (doneBytes: Long, totalBytes: Long) -> Unit,
    ): ImportedFile = withContext(ioDispatcher) {
        val source = uri.toUri()
        val resolver = context.contentResolver
        val (displayName, declaredSize) = query(source)

        val info = try {
            resolver.openInputStream(source)?.buffered()?.use { GgufReader.read(it) }
                ?: throw DownloadFailure(TransferError.CannotOpen)
        } catch (_: EOFException) {
            throw DownloadFailure(TransferError.NotGguf)
        } catch (_: NotGgufException) {
            throw DownloadFailure(TransferError.NotGguf)
        }

        if (declaredSize > 0 && !storage.hasRoomFor(id, declaredSize)) {
            throw DownloadFailure(TransferError.NotEnoughSpace)
        }

        val partial = storage.partialFile(id)
        try {
            var copied = 0L
            resolver.openInputStream(source)?.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        copied += read
                        onProgress(copied, declaredSize)
                    }
                }
            } ?: throw DownloadFailure(TransferError.CannotOpen)

            val target = storage.modelFile(id)
            target.delete()
            if (!partial.renameTo(target)) throw DownloadFailure(TransferError.CannotSave)
            ImportedFile(displayName, copied, info)
        } catch (e: Throwable) {
            partial.delete()
            throw e
        }
    }

    private fun query(uri: Uri): Pair<String, Long> {
        var name = uri.lastPathSegment ?: "model.gguf"
        var size = -1L
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }
                    ?.let { cursor.getString(it) }?.let { name = it }
                cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let { size = cursor.getLong(it) }
            }
        }
        return name to size
    }

    private companion object {
        const val COPY_BUFFER_BYTES = 256 * 1024
    }
}
