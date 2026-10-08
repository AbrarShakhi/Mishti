package com.abrarshakhi.mishti.features.models.data.catalog

import com.abrarshakhi.mishti.features.models.domain.model.Catalog
import com.abrarshakhi.mishti.features.models.domain.model.CatalogProblem
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.repository.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.EnumMap
import java.util.concurrent.ConcurrentHashMap

class DefaultCatalogRepository(
    private val store: CatalogStore,
    private val fetcher: CatalogFetcher,
    private val device: () -> DeviceProfile,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CatalogRepository {

    private val states = EnumMap<CatalogSource, MutableStateFlow<CatalogState>>(
        CatalogSource::class.java,
    ).apply {
        CatalogSource.entries.forEach { put(it, MutableStateFlow(CatalogState.Loading(false))) }
    }

    private val locks = CatalogSource.entries.associateWith { Mutex() }
    private val loaded = ConcurrentHashMap.newKeySet<CatalogSource>()

    override fun state(source: CatalogSource): Flow<CatalogState> =
        states.getValue(source).asStateFlow()

    override suspend fun refresh(source: CatalogSource, force: Boolean) {
        val flow = states.getValue(source)
        locks.getValue(source).withLock {
            withContext(ioDispatcher) { loadSaved(source) }
            val current = flow.value
            val updatedAt = (current as? CatalogState.Ready)?.updatedAtMillis
            val fresh = updatedAt != null && clock() - updatedAt < FRESH_FOR_MILLIS
            if (fresh && !force) return

            flow.update { it.refreshing(true) }
            try {
                val cached = withContext(ioDispatcher) { store.read(source) }
                val result = fetcher.fetch(url(source), cached?.etag.takeIf { current is CatalogState.Ready })
                val now = clock()
                when (result) {
                    FetchResult.NotModified -> {
                        withContext(ioDispatcher) { store.touch(source, now) }
                        flow.update { state ->
                            if (state is CatalogState.Ready) {
                                state.copy(updatedAtMillis = now, isOffline = false, isRefreshing = false)
                            } else {
                                state.refreshing(false)
                            }
                        }
                    }
                    is FetchResult.Fresh -> {
                        val catalog = parse(source, result.text)
                        withContext(ioDispatcher) {
                            store.write(source, CachedCatalog(result.text, result.etag, now))
                        }
                        flow.value = CatalogState.Ready(catalog, now, isOffline = false, isRefreshing = false)
                    }
                }
            } catch (e: CancellationException) {
                flow.update { it.refreshing(false) }
                throw e
            } catch (e: Exception) {
                flow.update { state ->
                    when (state) {
                        is CatalogState.Ready -> state.copy(isOffline = e is IOException, isRefreshing = false)
                        else -> CatalogState.Unavailable(problemFor(e), isRefreshing = false)
                    }
                }
            }
        }
    }

    private fun loadSaved(source: CatalogSource) {
        if (!loaded.add(source)) return
        val bundled = store.bundled(source)?.let { text ->
            runCatching { parse(source, text) }.getOrNull()
        }
        val cached = store.read(source)
        val saved = cached?.let { runCatching { parse(source, it.text) }.getOrNull() }

        val state = when {
            saved != null && (bundled == null || saved.version >= bundled.version) ->
                CatalogState.Ready(saved, cached.fetchedAtMillis, isOffline = false, isRefreshing = false)
            bundled != null ->
                CatalogState.Ready(bundled, updatedAtMillis = null, isOffline = false, isRefreshing = false)
            else -> null
        }
        if (state != null) states.getValue(source).value = state
    }

    private fun parse(source: CatalogSource, text: String): Catalog = when (source) {
        CatalogSource.MistirBhandar -> MishtiCatalogParser.parse(text)
        CatalogSource.PocketPal -> PocketPalCatalogParser.parse(text, device())
    }

    private fun url(source: CatalogSource) = when (source) {
        CatalogSource.MistirBhandar -> CatalogUrls.MISTIR_BHANDAR
        CatalogSource.PocketPal -> CatalogUrls.POCKETPAL
    }

    private fun problemFor(error: Exception): CatalogProblem = when (error) {
        is CatalogFormatException -> CatalogProblem.Unreadable
        is IOException -> CatalogProblem.Offline
        else -> CatalogProblem.Unavailable
    }

    private fun CatalogState.refreshing(value: Boolean): CatalogState = when (this) {
        is CatalogState.Loading -> copy(isRefreshing = value)
        is CatalogState.Ready -> copy(isRefreshing = value)
        is CatalogState.Unavailable -> copy(isRefreshing = value)
    }

    companion object {
        const val FRESH_FOR_MILLIS = 6 * 60 * 60 * 1000L
    }
}
