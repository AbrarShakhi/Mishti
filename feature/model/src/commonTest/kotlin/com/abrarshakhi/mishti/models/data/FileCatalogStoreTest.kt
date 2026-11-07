package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.data.catalog.CachedCatalog
import com.abrarshakhi.mishti.models.data.catalog.FileCatalogStore
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileCatalogStoreTest {
    @Test
    fun nativeRawCatalogRemainsAvailableOfflineUntilReplacedByNewCache() {
        FakeFileSystem().use { fs ->
            val directory = "/catalogs".toPath()
            val store = FileCatalogStore(fs, directory)
            fs.sink(directory / "PocketPal.json").buffer().use { it.writeUtf8("native catalog") }
            val restored = store.read(CatalogSource.PocketPal)
            assertEquals("native catalog", restored?.text)
            assertNull(restored?.etag)
            val fresh = CachedCatalog("fresh catalog", "etag", 42)
            store.write(CatalogSource.PocketPal, fresh)
            assertEquals(fresh, store.read(CatalogSource.PocketPal))
            store.touch(CatalogSource.PocketPal, 43)
            assertEquals(fresh.copy(fetchedAtMillis = 43), store.read(CatalogSource.PocketPal))
        }
    }
}
