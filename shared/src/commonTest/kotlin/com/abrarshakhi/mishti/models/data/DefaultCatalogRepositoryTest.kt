package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.data.catalog.CachedCatalog
import com.abrarshakhi.mishti.models.data.catalog.CatalogFetcher
import com.abrarshakhi.mishti.models.data.catalog.CatalogStore
import com.abrarshakhi.mishti.models.data.catalog.DefaultCatalogRepository
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfile
import com.abrarshakhi.mishti.models.data.catalog.FetchResult
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.CatalogState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun mishti(version: String, vararg ids: String) = """
    {"schema_version": 1, "catalog_version": "$version", "models": [
    ${
    ids.joinToString(
        ",",
    ) {
        """{"id": "$it", "name": "$it", "hf_repo": "o/r", "hf_file": "$it.gguf", "size_bytes": 100, "sha256": "${
            "a".repeat(
                64,
            )
        }"}"""
    }
}
    ]}
""".trimIndent()

private class FakeStore(var saved: CachedCatalog? = null) : CatalogStore {
    override fun read(source: CatalogSource) = saved

    override fun write(source: CatalogSource, catalog: CachedCatalog) {
        saved = catalog
    }

    override fun touch(source: CatalogSource, fetchedAtMillis: Long) {
        saved = saved?.copy(fetchedAtMillis = fetchedAtMillis)
    }
}

private class FakeFetcher(var result: () -> FetchResult) : CatalogFetcher {
    var calls = 0
    var lastEtag: String? = null

    override suspend fun fetch(url: String, etag: String?): FetchResult {
        calls++
        lastEtag = etag
        return result()
    }
}

class DefaultCatalogRepositoryTest {
    private var now = 1_000_000_000L

    private fun TestScope.repository(store: CatalogStore, fetcher: CatalogFetcher) = DefaultCatalogRepository(
        store = store,
        fetcher = fetcher,
        device = { DeviceProfile(4_000_000_000L, null, null, null) },
        clock = { now },
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun `the cached catalog shows even when the network fails`() = runTest {
        val repo =
            repository(
                FakeStore(saved = CachedCatalog(mishti("2026-10-01.1", "a", "b"), null, 0)),
                FakeFetcher { throw IOException("offline") },
            )

        repo.refresh(CatalogSource.MishtirBhandar, force = false)

        val state = repo.state(CatalogSource.MishtirBhandar).first() as CatalogState.Ready
        assertEquals(listOf("a", "b"), state.catalog.models.map { it.id })
        assertTrue(state.isOffline)
    }

    @Test
    fun `a newer download replaces the cached list and is saved`() = runTest {
        val store = FakeStore(saved = CachedCatalog(mishti("2026-10-01.1", "a"), null, 0))
        val repo = repository(
            store,
            FakeFetcher { FetchResult.Fresh(mishti("2026-10-05.1", "a", "c"), "\"e1\"") },
        )

        repo.refresh(CatalogSource.MishtirBhandar, force = false)

        val state = repo.state(CatalogSource.MishtirBhandar).first() as CatalogState.Ready
        assertEquals(listOf("a", "c"), state.catalog.models.map { it.id })
        assertEquals(now, state.updatedAtMillis)
        assertEquals("\"e1\"", store.saved?.etag)
    }

    @Test
    fun `a first install without a connection reports the catalog as unavailable`() = runTest {
        val repo = repository(FakeStore(), FakeFetcher { throw IOException("offline") })
        repo.refresh(CatalogSource.MishtirBhandar, force = false)
        assertTrue(repo.state(CatalogSource.MishtirBhandar).first() is CatalogState.Unavailable)
    }

    @Test
    fun `a recently fetched list is not fetched again unless forced`() = runTest {
        val store = FakeStore(saved = CachedCatalog(mishti("2026-10-08.1", "a"), "\"e1\"", now))
        val fetcher = FakeFetcher { FetchResult.NotModified }
        val repo = repository(store, fetcher)

        now += 60_000L
        repo.refresh(CatalogSource.MishtirBhandar, force = false)
        assertEquals(0, fetcher.calls)

        repo.refresh(CatalogSource.MishtirBhandar, force = true)
        assertEquals(1, fetcher.calls)
        assertEquals("\"e1\"", fetcher.lastEtag)
        val state = repo.state(CatalogSource.MishtirBhandar).first() as CatalogState.Ready
        assertEquals(now, state.updatedAtMillis)
    }

    @Test
    fun `PocketPal without a saved copy is unavailable when offline`() = runTest {
        val repo = repository(FakeStore(), FakeFetcher { throw IOException("offline") })

        repo.refresh(CatalogSource.PocketPal, force = false)

        val state = repo.state(CatalogSource.PocketPal).first()
        assertTrue(state is CatalogState.Unavailable)
        assertEquals(com.abrarshakhi.mishti.models.domain.model.CatalogProblem.Offline, state.problem)
    }

    @Test
    fun `a broken download keeps the list already shown`() = runTest {
        val store = FakeStore(saved = CachedCatalog(mishti("2026-10-01.1", "a"), null, 0))
        val repo = repository(store, FakeFetcher { FetchResult.Fresh("<html>", null) })

        repo.refresh(CatalogSource.MishtirBhandar, force = false)

        val state = repo.state(CatalogSource.MishtirBhandar).first() as CatalogState.Ready
        assertEquals(listOf("a"), state.catalog.models.map { it.id })
        assertEquals(
            listOf("a"),
            com.abrarshakhi.mishti.models.data.catalog.MishtiCatalogParser.parse(store.saved!!.text).models.map {
                it.id
            },
        )
    }
}
