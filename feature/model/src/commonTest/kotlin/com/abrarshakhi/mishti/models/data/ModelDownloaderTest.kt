package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.domain.model.TransferError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ModelDownloaderTest {
    private val body = "model bytes"
    private val request =
        DownloadRequest(
            "model",
            "Model",
            "https://example.test/model",
            body.length.toLong(),
            body.encodeUtf8().sha256().hex(),
        )

    @Test
    fun resumesPartialFileAndCommitsOnlyVerifiedBytes() = runTest {
        FakeFileSystem().use { fs ->
            val storage = ModelStorage(fs, "/models".toPath()) { Long.MAX_VALUE }
            fs.sink(storage.partialFile("model")).buffer().use { it.writeUtf8(body.take(4)) }
            HttpClient(
                MockEngine { incoming ->
                    assertEquals("bytes=4-", incoming.headers[HttpHeaders.Range])
                    respond(
                        body.drop(4),
                        HttpStatusCode.PartialContent,
                        headersOf(HttpHeaders.ContentRange, "bytes 4-10/11"),
                    )
                },
            ).use { client ->
                ModelDownloader(client, storage, StandardTestDispatcher(testScheduler)).download(request) { }
            }
            assertEquals(body, fs.source(storage.modelFile("model")).buffer().use { it.readUtf8() })
            assertFalse(fs.exists(storage.partialFile("model")))
        }
    }

    @Test
    fun restartsFromZeroWhenServerIgnoresRange() = runTest {
        FakeFileSystem().use { fs ->
            val storage = ModelStorage(fs, "/models".toPath()) { Long.MAX_VALUE }
            fs.sink(storage.partialFile("model")).buffer().use { it.writeUtf8("wrong") }
            HttpClient(MockEngine { respond(body) }).use { client ->
                ModelDownloader(client, storage, StandardTestDispatcher(testScheduler)).download(request) { }
            }
            assertEquals(body, fs.source(storage.modelFile("model")).buffer().use { it.readUtf8() })
        }
    }

    @Test
    fun corruptDownloadCannotReplaceInstalledModel() = runTest {
        FakeFileSystem().use { fs ->
            val storage = ModelStorage(fs, "/models".toPath()) { Long.MAX_VALUE }
            fs.sink(storage.modelFile("model")).buffer().use { it.writeUtf8("installed") }
            HttpClient(MockEngine { respond("wrong bytes") }).use { client ->
                val failure = assertFailsWith<DownloadFailure> {
                    ModelDownloader(client, storage, StandardTestDispatcher(testScheduler)).download(request) { }
                }
                assertEquals(TransferError.VerificationFailed, failure.error)
            }
            assertEquals("installed", fs.source(storage.modelFile("model")).buffer().use { it.readUtf8() })
            assertFalse(fs.exists(storage.partialFile("model")))
        }
    }

    @Test
    fun completePartialFileIsVerifiedWithoutAnotherRequest() = runTest {
        FakeFileSystem().use { fs ->
            val storage = ModelStorage(fs, "/models".toPath()) { Long.MAX_VALUE }
            fs.sink(storage.partialFile("model")).buffer().use { it.writeUtf8(body) }
            HttpClient(MockEngine { error("A complete partial file should not need a range request") }).use { client ->
                ModelDownloader(client, storage, StandardTestDispatcher(testScheduler)).download(request) { }
            }
            assertEquals(body, fs.source(storage.modelFile("model")).buffer().use { it.readUtf8() })
        }
    }
}
