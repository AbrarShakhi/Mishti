package com.abrarshakhi.mishti.features.models

import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource

fun catalogModel(
    id: String,
    sizeBytes: Long = 400_000_000L,
    requiredRamBytes: Long = 3_200_000_000L,
    tags: Set<String> = emptySet(),
    groupId: String? = null,
    source: CatalogSource = CatalogSource.MistirBhandar,
    hfRepo: String = "owner/$id-GGUF",
    hfFile: String = "$id.gguf",
    name: String = id,
) = CatalogModel(
    id = id,
    source = source,
    name = name,
    description = null,
    publisher = "owner",
    parametersLabel = null,
    quantization = "Q4_K_M",
    hfRepo = hfRepo,
    hfFile = hfFile,
    sizeBytes = sizeBytes,
    sha256 = "0".repeat(64),
    requiredRamBytes = requiredRamBytes,
    contextLength = null,
    license = null,
    tags = tags,
    groupId = groupId,
)
