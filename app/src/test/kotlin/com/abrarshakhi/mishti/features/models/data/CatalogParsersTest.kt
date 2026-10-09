package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.features.models.data.catalog.CatalogFormatException
import com.abrarshakhi.mishti.features.models.data.catalog.DeviceProfile
import com.abrarshakhi.mishti.features.models.data.catalog.MishtiCatalogParser
import com.abrarshakhi.mishti.features.models.data.catalog.PocketPalCatalogParser
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogParsersTest {
    private val pocketPalJson =
        """
        {
          "platform": "android",
          "rules_version": "2026-09-24.1",
          "classifier": {
            "ram_bands": [
              {"id": "lt-4", "max_bytes": 4294967296},
              {"id": "4-6", "max_bytes": 6442450944},
              {"id": "6-plus", "max_bytes": null}
            ],
            "soc_model_to_class": {"SM8650": "flagship"},
            "hardware_to_class": {"lynx": "mid"},
            "tier_matrix": [
              {"ram_band": "lt-4", "soc_class": "budget", "tier": "low"},
              {"ram_band": "4-6", "soc_class": "budget", "tier": "low"},
              {"ram_band": "4-6", "soc_class": "mid", "tier": "mid"},
              {"ram_band": "6-plus", "soc_class": "flagship", "tier": "flagship"}
            ]
          },
          "tiers": {
            "low": {"candidates": [
              {"model": "qwen3-0.6b", "display_name": "Qwen3 0.6B", "quant": "q4_k_m",
               "hf_repo": "bartowski/Qwen_Qwen3-0.6B-GGUF", "hf_filename": "Qwen_Qwen3-0.6B-Q4_K_M.gguf",
               "params": 751632384, "size_bytes": 484220320, "sha256": "abc", "min_ram_gb": 1.3, "obs_tg": 11.9}
            ]},
            "mid": {"candidates": [
              {"model": "lfm2-vl-1.6b", "display_name": "LFM2-VL 1.6B", "quant": "q4_0",
               "hf_repo": "LiquidAI/LFM2-VL-1.6B-GGUF", "hf_filename": "LFM2-VL-1.6B-Q4_0.gguf",
               "size_bytes": 695750048, "sha256": "def", "multimodal": true},
              {"model": "qwen3-0.6b", "display_name": "Qwen3 0.6B", "quant": "q4_k_m",
               "hf_repo": "bartowski/Qwen_Qwen3-0.6B-GGUF", "hf_filename": "Qwen_Qwen3-0.6B-Q4_K_M.gguf",
               "size_bytes": 484220320, "sha256": "abc"},
              {"model": "qwen3-1.7b", "display_name": "Qwen3 1.7B", "quant": "q4_k_m",
               "hf_repo": "bartowski/Qwen_Qwen3-1.7B-GGUF", "hf_filename": "Qwen_Qwen3-1.7B-Q4_K_M.gguf",
               "size_bytes": 1282439584, "sha256": "ghi", "min_ram_gb": 3.0}
            ]},
            "high": {"candidates": []},
            "flagship": {"candidates": [
              {"model": "broken-entry"}
            ]}
          }
        }
        """.trimIndent()

    private fun device(
        ram: Long,
        hardware: String? = null,
        soc: String? = null,
    ) = DeviceProfile(totalRamBytes = ram, socModel = soc, hardware = hardware, board = null)

    @Test
    fun `the bundled Mistir Bhandar catalog parses and lists recommended models`() {
        val catalog = MishtiCatalogParser.parse(File("../hub/catalog.v1.json").readText())

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
            assertEquals("${model.id} has a sha256", 64, model.sha256.length)
            assertTrue("${model.id} has a size", model.sizeBytes > 0)
        }
    }

    @Test
    fun `the three original downloads keep their ids`() {
        val ids =
            MishtiCatalogParser.parse(File("../hub/catalog.v1.json").readText()).models.map { it.id }

        assertTrue(
            ids.containsAll(
                listOf(
                    "smollm2-360m-instruct-q4km",
                    "qwen2.5-0.5b-instruct-q4km",
                    "llama-3.2-1b-instruct-q4km"
                ),
            ),
        )
    }

    @Test(expected = CatalogFormatException::class)
    fun `a catalog from a newer schema is refused`() {
        MishtiCatalogParser.parse("""{"schema_version": 2, "catalog_version": "x", "models": []}""")
    }

    @Test(expected = CatalogFormatException::class)
    fun `text that is not json is refused`() {
        MishtiCatalogParser.parse("<html>Not found</html>")
    }

    @Test
    fun `PocketPal models appear once, under the lowest tier that lists them`() {
        val catalog = PocketPalCatalogParser.parse(pocketPalJson, device(3_800_000_000L))

        assertEquals(listOf("Qwen3 0.6B", "Qwen3 1.7B"), catalog.models.map { it.name })
        assertEquals("low", catalog.models.first().groupId)
        assertEquals("Q4_K_M", catalog.models.first().quantization)
    }

    @Test
    fun `PocketPal vision models and broken entries are left out`() {
        val catalog = PocketPalCatalogParser.parse(pocketPalJson, device(3_800_000_000L))

        assertFalse(catalog.models.any { "VL" in it.name })
        assertFalse(catalog.models.any { it.name == "broken-entry" })
    }

    @Test
    fun `the phone's tier comes from its memory and chip`() {
        assertEquals(
            "low",
            PocketPalCatalogParser.parse(pocketPalJson, device(3_800_000_000L)).deviceGroupId
        )
        assertEquals(
            "low",
            PocketPalCatalogParser.parse(pocketPalJson, device(5_000_000_000L)).deviceGroupId
        )
        assertEquals(
            "mid",
            PocketPalCatalogParser.parse(
                pocketPalJson,
                device(5_000_000_000L, hardware = "lynx")
            ).deviceGroupId,
        )
        assertEquals(
            "flagship",
            PocketPalCatalogParser.parse(
                pocketPalJson,
                device(12_000_000_000L, soc = "SM8650")
            ).deviceGroupId,
        )
    }

    @Test
    fun `recommendations are the models listed for the phone's tier`() {
        val catalog =
            PocketPalCatalogParser.parse(pocketPalJson, device(5_000_000_000L, hardware = "lynx"))

        assertEquals(2, catalog.recommendedIds.size)
    }

    @Test
    fun `PocketPal's memory figure is raised to what Mishti needs to run the file`() {
        val small =
            PocketPalCatalogParser.parse(pocketPalJson, device(3_800_000_000L)).models.first()

        assertTrue(small.requiredRamBytes >= 3_200_000_000L)
    }
}
