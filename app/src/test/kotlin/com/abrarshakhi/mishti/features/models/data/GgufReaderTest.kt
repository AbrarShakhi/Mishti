package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.features.models.data.gguf.GgufReader
import com.abrarshakhi.mishti.features.models.data.gguf.NotGgufException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException

class GgufReaderTest {
    private class GgufWriter {
        private val out = ByteArrayOutputStream()
        private var kvCount = 0
        private val body = ByteArrayOutputStream()

        private fun ByteArrayOutputStream.u32(v: Long) = repeat(4) {
            write(
                (
                    (v shr (8 * it)) and
                        0xFF
                    ).toInt(),
            )
        }

        private fun ByteArrayOutputStream.u64(v: Long) = repeat(8) {
            write(
                (
                    (v shr (8 * it)) and
                        0xFF
                    ).toInt(),
            )
        }

        private fun ByteArrayOutputStream.str(s: String) {
            val bytes = s.toByteArray()
            u64(bytes.size.toLong())
            write(bytes)
        }

        fun string(key: String, value: String) = apply {
            kvCount++
            body.str(key)
            body.u32(8)
            body.str(value)
        }

        fun u32(key: String, value: Long) = apply {
            kvCount++
            body.str(key)
            body.u32(4)
            body.u32(value)
        }

        fun stringArray(key: String, values: List<String>) = apply {
            kvCount++
            body.str(key)
            body.u32(9)
            body.u32(8)
            body.u64(values.size.toLong())
            values.forEach { body.str(it) }
        }

        fun float32Array(key: String, count: Int) = apply {
            kvCount++
            body.str(key)
            body.u32(9)
            body.u32(6)
            body.u64(count.toLong())
            repeat(count) { body.u32(0) }
        }

        fun bytes(): ByteArray {
            out.write("GGUF".toByteArray())
            out.u32(3)
            out.u64(0)
            out.u64(kvCount.toLong())
            out.write(body.toByteArray())
            return out.toByteArray()
        }
    }

    @Test
    fun `reads the name, architecture, size, quantisation and context`() {
        val bytes =
            GgufWriter()
                .string("general.architecture", "qwen3")
                .string("general.name", "Qwen3 0.6B")
                .string("general.size_label", "0.6B")
                .u32("general.file_type", 15)
                .u32("qwen3.context_length", 40960)
                .stringArray("tokenizer.ggml.tokens", List(1000) { "token$it" })
                .float32Array("tokenizer.ggml.scores", 1000)
                .string("general.license", "apache-2.0")
                .bytes()

        val info = GgufReader.read(ByteArrayInputStream(bytes))

        assertEquals("Qwen3 0.6B", info.name)
        assertEquals("qwen3", info.architecture)
        assertEquals("0.6B", info.sizeLabel)
        assertEquals("Q4_K_M", info.quantization)
        assertEquals(40960, info.contextLength)
        assertEquals("apache-2.0", info.license)
    }

    @Test
    fun `missing keys are simply absent`() {
        val info = GgufReader.read(
            ByteArrayInputStream(
                GgufWriter().string(
                    "general.architecture",
                    "llama",
                ).bytes(),
            ),
        )

        assertEquals("llama", info.architecture)
        assertNull(info.name)
        assertNull(info.quantization)
        assertNull(info.contextLength)
    }

    @Test(expected = NotGgufException::class)
    fun `a file without the GGUF magic is rejected`() {
        GgufReader.read(ByteArrayInputStream("PK\u0003\u0004 a zip file".toByteArray()))
    }

    @Test(expected = EOFException::class)
    fun `a truncated header is reported`() {
        val bytes = GgufWriter().string("general.name", "Cut short").bytes()
        GgufReader.read(ByteArrayInputStream(bytes.copyOf(bytes.size - 4)))
    }
}
