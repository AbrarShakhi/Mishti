package com.abrarshakhi.mishti.features.models.data

import android.content.Context
import android.os.StatFs
import java.io.File
import java.security.MessageDigest

class ModelStorage(private val context: Context) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").apply { mkdirs() }

    fun modelFile(id: String): File = File(modelsDir, "$id.gguf")

    fun partialFile(id: String): File = File(modelsDir, "$id.gguf.part")

    fun isPresent(id: String): Boolean = modelFile(id).let { it.isFile && it.length() > 0 }

    fun sizeOnDisk(id: String): Long = modelFile(id).takeIf { it.isFile }?.length() ?: 0L

    fun partialBytes(id: String): Long = partialFile(id).takeIf { it.isFile }?.length() ?: 0L

    fun presentIds(): List<String> =
        modelsDir.listFiles { file -> file.isFile && file.name.endsWith(".gguf") }
            ?.map { it.name.removeSuffix(".gguf") }
            .orEmpty()

    fun delete(id: String): Boolean {
        partialFile(id).delete()
        return modelFile(id).delete()
    }

    fun usedBytes(): Long =
        modelsDir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

    fun availableBytes(): Long = runCatching {
        val stat = StatFs(context.filesDir.absolutePath)
        stat.availableBlocksLong * stat.blockSizeLong
    }.getOrDefault(0L)

    fun hasRoomFor(id: String, sizeBytes: Long): Boolean {
        val needed = sizeBytes - partialBytes(id) + STORAGE_HEADROOM_BYTES
        return availableBytes() >= needed
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val STORAGE_HEADROOM_BYTES = 250L * 1024 * 1024
    }
}
