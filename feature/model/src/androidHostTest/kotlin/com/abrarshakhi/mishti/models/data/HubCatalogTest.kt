package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.data.catalog.MishtiCatalogParser
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubCatalogTest {
    @Test
    fun `the published Mistir Bhandar catalog parses and lists recommended models`() = runTest {
        val catalog = MishtiCatalogParser.parse(
            File("../../hub/catalog.v1.json").readText(),
        )

        assertEquals(CatalogSource.MishtirBhandar, catalog.source)
        assertTrue(catalog.models.size >= 3)
        assertTrue(catalog.recommendedIds.isNotEmpty())
        assertEquals(
            catalog.models.size,
            catalog.models
                .map { it.id }
                .toSet()
                .size,
        )
        catalog.models.forEach { model ->
            assertEquals(64, model.sha256.length, "${model.id} has a sha256")
            assertTrue(model.sizeBytes > 0, "${model.id} has a size")
        }
    }

    @Test
    fun `the three original downloads keep their ids`() = runTest {
        val ids =
            MishtiCatalogParser.parse(
                File("../../hub/catalog.v1.json").readText(),
            ).models.map {
                it.id
            }

        assertTrue(
            ids.containsAll(
                listOf(
                    "smollm2-360m-instruct-q4km",
                    "qwen2.5-0.5b-instruct-q4km",
                    "llama-3.2-1b-instruct-q4km",
                ),
            ),
        )
    }
}
