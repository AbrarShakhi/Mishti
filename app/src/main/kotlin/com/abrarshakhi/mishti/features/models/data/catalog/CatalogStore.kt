package com.abrarshakhi.mishti.features.models.data.catalog

import android.content.Context
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import java.io.File
import java.util.Properties

data class CachedCatalog(
    val text: String,
    val etag: String?,
    val fetchedAtMillis: Long,
)

interface CatalogStore {
    fun bundled(source: CatalogSource): String?

    fun read(source: CatalogSource): CachedCatalog?

    fun write(
        source: CatalogSource,
        catalog: CachedCatalog,
    )

    fun touch(
        source: CatalogSource,
        fetchedAtMillis: Long,
    )
}

class FileCatalogStore(
    private val context: Context,
) : CatalogStore {
    private val dir: File
        get() = File(context.filesDir, "catalogs").apply { mkdirs() }

    private fun body(source: CatalogSource) = File(dir, "${source.name}.json")

    private fun meta(source: CatalogSource) = File(dir, "${source.name}.properties")

    override fun bundled(source: CatalogSource): String? =
        when (source) {
            CatalogSource.MishtirBhandar -> {
                runCatching {
                    context.assets
                        .open(BUNDLED_CATALOG_ASSET)
                        .bufferedReader()
                        .use { it.readText() }
                }.getOrNull()
            }

            CatalogSource.PocketPal -> {
                null
            }
        }

    override fun read(source: CatalogSource): CachedCatalog? =
        runCatching {
            val text = body(source).takeIf { it.isFile }?.readText() ?: return null
            val props = Properties().apply { meta(source).inputStream().use { load(it) } }
            CachedCatalog(
                text = text,
                etag = props.getProperty(KEY_ETAG),
                fetchedAtMillis = props.getProperty(KEY_FETCHED_AT)?.toLongOrNull() ?: 0L,
            )
        }.getOrNull()

    override fun write(
        source: CatalogSource,
        catalog: CachedCatalog,
    ) {
        val temp = File(dir, "${source.name}.json.tmp")
        temp.writeText(catalog.text)
        temp.renameTo(body(source))
        writeMeta(source, catalog.etag, catalog.fetchedAtMillis)
    }

    override fun touch(
        source: CatalogSource,
        fetchedAtMillis: Long,
    ) {
        writeMeta(source, read(source)?.etag, fetchedAtMillis)
    }

    private fun writeMeta(
        source: CatalogSource,
        etag: String?,
        fetchedAtMillis: Long,
    ) {
        val props = Properties()
        etag?.let { props.setProperty(KEY_ETAG, it) }
        props.setProperty(KEY_FETCHED_AT, fetchedAtMillis.toString())
        meta(source).outputStream().use { props.store(it, null) }
    }

    private companion object {
        const val BUNDLED_CATALOG_ASSET = "catalog.v1.json"
        const val KEY_ETAG = "etag"
        const val KEY_FETCHED_AT = "fetchedAt"
    }
}
