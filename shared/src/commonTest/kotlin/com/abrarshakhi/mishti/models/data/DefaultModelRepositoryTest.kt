@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.core.testing.FakeAppPreferences
import com.abrarshakhi.mishti.models.catalogModel
import com.abrarshakhi.mishti.models.data.local.InstalledModelDao
import com.abrarshakhi.mishti.models.data.local.InstalledModelEntity
import com.abrarshakhi.mishti.models.domain.model.TransferError
import com.abrarshakhi.mishti.models.domain.model.TransferStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.Source
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultModelRepositoryTest {
    private class Dao : InstalledModelDao {
        val rows = MutableStateFlow<List<InstalledModelEntity>>(emptyList())
        override fun observeAll() = rows
        override suspend fun all() = rows.value
        override suspend fun byId(id: String) = rows.value.find { it.id == id }
        override suspend fun upsert(model: InstalledModelEntity) {
            rows.value =
                rows.value.filterNot { it.id == model.id } + model
        }
        override suspend fun delete(id: String) {
            rows.value = rows.value.filterNot { it.id == id }
        }
    }
    private class ImportSource(private val bytes: Buffer) : ModelImportSource {
        override fun metadata(uri: String) = ImportMetadata("imported.gguf", bytes.size)
        override fun open(uri: String): Source = bytes.clone()
    }
    private class Fixture(val scope: TestScope, val client: HttpClient, importBytes: Buffer = Buffer()) {
        val fs = FakeFileSystem()
        val storage = ModelStorage(fs, "/models".toPath()) { Long.MAX_VALUE }
        val preferences = FakeAppPreferences()
        val dao = Dao()
        val dispatcher = StandardTestDispatcher(scope.testScheduler)
        val repository = DefaultModelRepository(
            dao,
            storage,
            ModelDownloader(client, storage, dispatcher),
            ModelImporter(ImportSource(importBytes), storage, dispatcher),
            preferences,
            scope,
            knownModels = { emptyList() },
            ioDispatcher = dispatcher,
        )
    }

    @Test
    fun duplicateDownloadRunsOnceAndSelectionSuppliesChatWithInstalledPath() = runTest {
        var requests = 0
        val body = "downloaded model"
        HttpClient(
            MockEngine {
                requests++
                respond(body)
            },
        ).use { client ->
            val fixture = Fixture(this, client)
            val model = catalogModel(
                "model",
            ).copy(sizeBytes = body.length.toLong(), sha256 = body.encodeUtf8().sha256().hex())
            fixture.repository.download(model)
            fixture.repository.download(model)
            fixture.repository.shelf.first { it.any { model -> model.id == "model" } }
            assertEquals(1, requests)
            assertEquals(listOf("model"), fixture.repository.shelf.first().map { it.id })
            fixture.repository.select("model")
            val handle = PreferencesSelectedModelSource(
                fixture.preferences,
                fixture.dao,
                fixture.storage,
            ).selected().first()
            assertEquals("/models/model.gguf", handle?.path)
            assertTrue(fixture.repository.transfers.first().isEmpty())
            fixture.repository.delete("model")
            assertNull(fixture.preferences.selectedModelId.first())
            assertFalse(fixture.fs.exists(fixture.storage.modelFile("model")))
            assertTrue(fixture.dao.all().isEmpty())
        }
    }

    @Test
    fun invalidImportLeavesNoInstalledRowOrPartialFile() = runTest {
        HttpClient(MockEngine { error("Import must not use the network") }).use { client ->
            val fixture = Fixture(this, client, Buffer().writeUtf8("invalid model"))
            fixture.repository.import("document")
            advanceUntilIdle()
            assertTrue(fixture.dao.all().isEmpty())
            assertTrue(fixture.fs.list("/models".toPath()).isEmpty())
            val failed = fixture.repository.transfers.first().single().status as TransferStatus.Failed
            assertEquals(TransferError.NotGguf, failed.error)
        }
    }

    @Test
    fun cancellingDownloadNeverInstallsItAndAllowsRetry() = runTest {
        var requests = 0
        val started = Channel<Unit>(Channel.UNLIMITED)
        HttpClient(
            MockEngine {
                requests++
                started.send(Unit)
                awaitCancellation()
            },
        ).use { client ->
            val fixture = Fixture(this, client)
            val model = catalogModel("model")
            fixture.repository.download(model)
            started.receive()
            fixture.repository.cancel("model")
            advanceUntilIdle()
            assertTrue(fixture.repository.transfers.first().isEmpty())
            assertTrue(fixture.dao.all().isEmpty())
            fixture.repository.download(model)
            started.receive()
            assertEquals(2, requests)
            fixture.repository.cancelAll()
            advanceUntilIdle()
        }
    }
}
