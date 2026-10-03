package com.abrarshakhi.mishti.features.models.data

import android.content.Context
import android.os.StatFs
import com.abrarshakhi.mishti.features.models.domain.model.LlmModel
import java.io.File
import java.security.MessageDigest

class ModelStorage(private val context: Context) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").apply { mkdirs() }

    fun modelFile(model: LlmModel): File = File(modelsDir, "${model.id}.gguf")

    fun partialFile(model: LlmModel): File = File(modelsDir, "${model.id}.gguf.part")

    fun isDownloaded(model: LlmModel): Boolean = modelFile(model).let { it.isFile && it.length() > 0 }

    fun sizeOnDisk(model: LlmModel): Long = modelFile(model).takeIf { it.isFile }?.length() ?: 0L

    fun partialBytes(model: LlmModel): Long =
        partialFile(model).takeIf { it.isFile }?.length() ?: 0L

    fun delete(model: LlmModel): Boolean {
        partialFile(model).delete()
        return modelFile(model).delete()
    }

    fun usedBytes(): Long =
        modelsDir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

    fun availableBytes(): Long = runCatching {
        val stat = StatFs(context.filesDir.absolutePath)
        stat.availableBlocksLong * stat.blockSizeLong
    }.getOrDefault(0L)

    fun hasRoomFor(model: LlmModel): Boolean {
        val needed = model.sizeBytes - partialBytes(model) + STORAGE_HEADROOM_BYTES
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
