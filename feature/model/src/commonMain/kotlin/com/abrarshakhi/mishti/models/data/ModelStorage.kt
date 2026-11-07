package com.abrarshakhi.mishti.models.data

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.FileSystem
import okio.HashingSource
import okio.Path
import okio.buffer
import okio.use

class ModelStorage(val fileSystem: FileSystem, private val directory: Path, private val freeBytes: () -> Long) {
    init {
        fileSystem.createDirectories(directory)
    }

    private fun file(id: String, suffix: String): Path {
        require(id.isNotBlank() && id != "." && id != ".." && id.none { it == '/' || it == '\\' || it == '\u0000' }) {
            "Invalid model id"
        }
        return directory / "$id$suffix"
    }

    fun modelFile(id: String): Path = file(id, ".gguf")
    fun partialFile(id: String): Path = file(id, ".gguf.part")
    fun isPresent(id: String): Boolean = sizeOnDisk(id) > 0
    fun sizeOnDisk(id: String): Long = size(modelFile(id))
    fun partialBytes(id: String): Long = size(partialFile(id))
    private fun size(path: Path): Long = fileSystem.metadataOrNull(path)?.takeIf { it.isRegularFile }?.size ?: 0
    fun presentIds(): List<String> = fileSystem.list(directory)
        .filter { it.name.endsWith(".gguf") && size(it) > 0 }
        .map { it.name.removeSuffix(".gguf") }
    fun delete(id: String) {
        fileSystem.delete(partialFile(id), mustExist = false)
        fileSystem.delete(modelFile(id), mustExist = false)
    }
    fun usedBytes(): Long = fileSystem.list(directory).sumOf { size(it) }
    fun availableBytes(): Long = freeBytes().coerceAtLeast(0)
    fun hasRoomFor(id: String, sizeBytes: Long): Boolean =
        availableBytes() >= (sizeBytes - partialBytes(id)).coerceAtLeast(0) + STORAGE_HEADROOM_BYTES

    suspend fun sha256(path: Path): String {
        val hashing = HashingSource.sha256(fileSystem.source(path))
        hashing.buffer().use { source ->
            val buffer = okio.Buffer()
            while (source.read(buffer, 128 * 1024L) != -1L) {
                currentCoroutineContext().ensureActive()
                buffer.clear()
            }
        }
        return hashing.hash.hex()
    }

    fun commit(id: String) {
        fileSystem.atomicMove(partialFile(id), modelFile(id))
    }

    private companion object {
        const val STORAGE_HEADROOM_BYTES = 250L * 1024 * 1024
    }
}
