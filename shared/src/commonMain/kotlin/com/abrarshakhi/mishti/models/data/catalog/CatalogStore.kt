package com.abrarshakhi.mishti.models.data.catalog

import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

@Serializable
data class CachedCatalog(val text: String, val etag: String?, val fetchedAtMillis: Long)

interface CatalogStore {
    fun read(source: CatalogSource): CachedCatalog?
    fun write(source: CatalogSource, catalog: CachedCatalog)
    fun touch(source: CatalogSource, fetchedAtMillis: Long)
}

class FileCatalogStore(private val fileSystem: FileSystem, private val directory: Path) : CatalogStore {
    init {
        fileSystem.createDirectories(directory)
    }
    private fun path(source: CatalogSource): Path = directory / "${source.name}.cache.json"
    override fun read(source: CatalogSource): CachedCatalog? = runCatching {
        fileSystem.source(path(source)).buffer().use { Json.decodeFromString<CachedCatalog>(it.readUtf8()) }
    }.getOrNull() ?: runCatching {
        fileSystem.source(directory / "${source.name}.json").buffer().use {
            CachedCatalog(it.readUtf8(), etag = null, fetchedAtMillis = 0)
        }
    }.getOrNull()
    override fun write(source: CatalogSource, catalog: CachedCatalog) {
        val temporary = directory / "${source.name}.tmp"
        fileSystem.sink(temporary).buffer().use { it.writeUtf8(Json.encodeToString(catalog)) }
        fileSystem.atomicMove(temporary, path(source))
    }
    override fun touch(source: CatalogSource, fetchedAtMillis: Long) {
        read(source)?.let { write(source, it.copy(fetchedAtMillis = fetchedAtMillis)) }
    }
}
