package com.abrarshakhi.mishti.models.data.gguf

import okio.BufferedSource

data class GgufInfo(
    val name: String?,
    val architecture: String?,
    val sizeLabel: String?,
    val quantization: String?,
    val contextLength: Int?,
    val license: String?,
)

class NotGgufException : Exception("This file is not a GGUF model.")

object GgufReader {
    private const val MAGIC = 0x46554747
    private const val MAX_STRING_BYTES = 64L * 1024 * 1024

    fun read(input: BufferedSource): GgufInfo {
        val stream = LittleEndianStream(input)
        if (stream.u32() != MAGIC.toLong()) throw NotGgufException()
        val version = stream.u32()
        if (version !in 2L..3L) throw NotGgufException()
        stream.u64()
        val kvCount = stream.u64()
        if (kvCount < 0 || kvCount > 1_000_000) throw NotGgufException()

        val values = mutableMapOf<String, Any>()
        for (i in 0 until kvCount) {
            val key = stream.string()
            val type = stream.u32().toInt()
            val value =
                stream.value(type, keep = key in WantedKeys || key.endsWith(".context_length"))
            if (value != null) values[key] = value
        }

        val architecture = values["general.architecture"] as? String
        return GgufInfo(
            name = values["general.name"] as? String,
            architecture = architecture,
            sizeLabel = values["general.size_label"] as? String,
            quantization = (values["general.file_type"] as? Number)?.toInt()?.let(::fileTypeName),
            contextLength = architecture?.let {
                (values["$it.context_length"] as? Number)?.toInt()
            },
            license = values["general.license"] as? String,
        )
    }

    private val WantedKeys =
        setOf(
            "general.name",
            "general.architecture",
            "general.size_label",
            "general.file_type",
            "general.license",
        )

    private fun LittleEndianStream.value(type: Int, keep: Boolean): Any? = when (type) {
        0, 1, 7 -> {
            u8().takeIf { keep }
        }

        2, 3 -> {
            skip(2).let { null }
        }

        4 -> {
            u32().takeIf { keep }
        }

        5 -> {
            u32().toInt().takeIf { keep }
        }

        6 -> {
            skip(4).let { null }
        }

        8 -> {
            if (keep) string() else skipString().let { null }
        }

        9 -> {
            val itemType = u32().toInt()
            val count = u64()
            if (itemType == 9 || count < 0 || count > 100_000_000) throw NotGgufException()
            for (i in 0 until count) value(itemType, keep = false)
            null
        }

        10, 11 -> {
            u64().takeIf { keep }
        }

        12 -> {
            skip(8).let { null }
        }

        else -> {
            throw NotGgufException()
        }
    }

    private fun LittleEndianStream.string(): String {
        val length = u64()
        if (length < 0 || length > MAX_STRING_BYTES) throw NotGgufException()
        return bytes(length.toInt()).decodeToString()
    }

    private fun LittleEndianStream.skipString() {
        val length = u64()
        if (length < 0 || length > MAX_STRING_BYTES) throw NotGgufException()
        skip(length)
    }

    fun fileTypeName(fileType: Int): String? = when (fileType) {
        0 -> "F32"
        1 -> "F16"
        2 -> "Q4_0"
        3 -> "Q4_1"
        7 -> "Q8_0"
        8 -> "Q5_0"
        9 -> "Q5_1"
        10 -> "Q2_K"
        11 -> "Q3_K_S"
        12 -> "Q3_K_M"
        13 -> "Q3_K_L"
        14 -> "Q4_K_S"
        15 -> "Q4_K_M"
        16 -> "Q5_K_S"
        17 -> "Q5_K_M"
        18 -> "Q6_K"
        19 -> "IQ2_XXS"
        20 -> "IQ2_XS"
        21 -> "Q2_K_S"
        22 -> "IQ3_XS"
        23 -> "IQ3_XXS"
        24 -> "IQ1_S"
        25 -> "IQ4_NL"
        26 -> "IQ3_S"
        27 -> "IQ3_M"
        28 -> "IQ2_S"
        29 -> "IQ2_M"
        30 -> "IQ4_XS"
        31 -> "IQ1_M"
        32 -> "BF16"
        36 -> "TQ1_0"
        37 -> "TQ2_0"
        else -> null
    }
}

private class LittleEndianStream(private val input: BufferedSource) {
    fun u8(): Long = input.readByte().toLong() and 0xff
    fun u32(): Long = input.readIntLe().toLong() and 0xffffffffL
    fun u64(): Long = input.readLongLe()
    fun bytes(count: Int): ByteArray = input.readByteArray(count.toLong())
    fun skip(count: Long) {
        input.skip(count)
    }
}
