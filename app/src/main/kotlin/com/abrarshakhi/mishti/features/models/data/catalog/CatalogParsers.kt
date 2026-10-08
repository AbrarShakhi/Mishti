package com.abrarshakhi.mishti.features.models.data.catalog

import com.abrarshakhi.mishti.features.models.domain.model.Catalog
import com.abrarshakhi.mishti.features.models.domain.model.CatalogGroup
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.estimatedRamBytes
import com.abrarshakhi.mishti.features.models.domain.model.parametersLabel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.math.max
import kotlin.math.roundToLong

class CatalogFormatException(message: String) : Exception(message)

data class DeviceProfile(
    val totalRamBytes: Long?,
    val socModel: String?,
    val hardware: String?,
    val board: String?,
)

const val MISHTI_SCHEMA_VERSION = 1

private const val GIB = 1_073_741_824.0

object MishtiCatalogParser {

    fun parse(text: String): Catalog {
        val root = parseObject(text)
        val schema = root.long("schema_version")
            ?: throw CatalogFormatException("The catalog has no schema version.")
        if (schema.toInt() != MISHTI_SCHEMA_VERSION) {
            throw CatalogFormatException("Unsupported catalog schema $schema.")
        }
        val version = root.string("catalog_version")
            ?: throw CatalogFormatException("The catalog has no version.")
        val models = root.array("models").mapNotNull { it.asObject()?.toModel() }
        if (models.isEmpty()) throw CatalogFormatException("The catalog lists no models.")

        return Catalog(
            source = CatalogSource.MistirBhandar,
            version = version,
            models = models,
            recommendedIds = models.filter { "recommended" in it.tags }.map { it.id },
        )
    }

    private fun JsonObject.toModel(): CatalogModel? {
        val id = string("id") ?: return null
        val repo = string("hf_repo") ?: return null
        val file = string("hf_file") ?: return null
        val size = long("size_bytes") ?: return null
        val sha = string("sha256") ?: return null
        return CatalogModel(
            id = id,
            source = CatalogSource.MistirBhandar,
            name = string("name") ?: file.removeSuffix(".gguf"),
            description = string("description"),
            publisher = string("publisher") ?: repo.substringBefore('/'),
            parametersLabel = parametersLabel(long("parameters")),
            quantization = string("quantization") ?: "",
            hfRepo = repo,
            hfFile = file,
            sizeBytes = size,
            sha256 = sha,
            requiredRamBytes = long("min_ram_bytes") ?: estimatedRamBytes(size),
            contextLength = long("context_length")?.toInt(),
            license = string("license"),
            tags = array("tags").mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.toSet(),
            groupId = null,
        )
    }
}

object PocketPalCatalogParser {

    private val tierOrder = listOf("low", "mid", "high", "flagship")

    fun parse(text: String, device: DeviceProfile): Catalog {
        val root = parseObject(text)
        if (root.string("platform") != "android") {
            throw CatalogFormatException("PocketPal's list is not for Android.")
        }
        val version = root.string("rules_version") ?: "unknown"
        val tiers = root["tiers"]?.asObject()
            ?: throw CatalogFormatException("PocketPal's list has no tiers.")

        val byFile = LinkedHashMap<String, CatalogModel>()
        val tierModels = mutableMapOf<String, List<String>>()

        tierOrder.forEach { tier ->
            val candidates = tiers[tier]?.asObject()?.array("candidates").orEmpty()
            val ids = candidates.mapNotNull { element ->
                val model = element.asObject()?.toModel(tier) ?: return@mapNotNull null
                val key = "${model.hfRepo}/${model.hfFile}"
                byFile.getOrPut(key) { model }.id
            }
            tierModels[tier] = ids.distinct()
        }
        if (byFile.isEmpty()) throw CatalogFormatException("PocketPal's list has no models.")

        val deviceTier = root["classifier"]?.asObject()?.let { classify(it, device) }

        return Catalog(
            source = CatalogSource.PocketPal,
            version = version,
            models = byFile.values.toList(),
            groups = tierOrder.filter { tierModels[it].orEmpty().isNotEmpty() }
                .map { CatalogGroup(it) },
            recommendedIds = deviceTier?.let { tierModels[it] }.orEmpty(),
            deviceGroupId = deviceTier,
        )
    }

    private fun JsonObject.toModel(tier: String): CatalogModel? {
        val key = string("model") ?: return null
        if (key.endsWith("-vl") || "-vl-" in key) return null
        val repo = string("hf_repo") ?: return null
        val file = string("hf_filename") ?: return null
        val size = long("size_bytes") ?: return null
        val sha = string("sha256") ?: return null
        val quant = string("quant")?.uppercase() ?: ""
        val minRam = double("min_ram_gb")?.let { (it * GIB).roundToLong() } ?: 0L
        return CatalogModel(
            id = "pocketpal-" + "$key-$quant".lowercase().replace(Regex("[^a-z0-9.]+"), "-"),
            source = CatalogSource.PocketPal,
            name = string("display_name") ?: key,
            description = null,
            publisher = repo.substringBefore('/'),
            parametersLabel = parametersLabel(long("params")),
            quantization = quant,
            hfRepo = repo,
            hfFile = file,
            sizeBytes = size,
            sha256 = sha,
            requiredRamBytes = max(minRam, estimatedRamBytes(size)),
            contextLength = null,
            license = null,
            tags = buildSet {
                if (boolean("multimodal") == true) add("multimodal")
                if (boolean("native_low_bit") == true) add("low-bit")
            },
            groupId = tier,
            typicalTokensPerSecond = double("obs_tg"),
        )
    }

    fun classify(classifier: JsonObject, device: DeviceProfile): String? {
        val ram = device.totalRamBytes ?: return null
        val band = classifier.array("ram_bands").mapNotNull { it.asObject() }.firstOrNull { band ->
            val max = band.long("max_bytes")
            max == null || ram <= max
        }?.string("id") ?: return null

        val socClass = listOfNotNull(
            device.socModel?.let { classifier["soc_model_to_class"]?.asObject()?.string(it) },
            device.hardware?.let { classifier["hardware_to_class"]?.asObject()?.string(it) },
            device.board?.let { classifier["hardware_to_class"]?.asObject()?.string(it) },
        ).firstOrNull() ?: "budget"

        return classifier.array("tier_matrix").mapNotNull { it.asObject() }.firstOrNull {
            it.string("ram_band") == band && it.string("soc_class") == socClass
        }?.string("tier")
    }
}

private val json = Json { ignoreUnknownKeys = true }

private fun parseObject(text: String): JsonObject =
    runCatching { json.parseToJsonElement(text) }.getOrNull()?.asObject()
        ?: throw CatalogFormatException("The catalog is not valid JSON.")

private fun JsonElement.asObject(): JsonObject? = this as? JsonObject

private fun JsonObject.primitive(key: String): JsonPrimitive? =
    this[key]?.takeIf { it !is JsonNull } as? JsonPrimitive

private fun JsonObject.string(key: String): String? =
    primitive(key)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }

private fun JsonObject.long(key: String): Long? =
    primitive(key)?.let { it.longOrNull ?: it.doubleOrNull?.roundToLong() }

private fun JsonObject.double(key: String): Double? = primitive(key)?.doubleOrNull

private fun JsonObject.boolean(key: String): Boolean? = primitive(key)?.booleanOrNull

private fun JsonObject.array(key: String): List<JsonElement> = (this[key] as? JsonArray).orEmpty()
