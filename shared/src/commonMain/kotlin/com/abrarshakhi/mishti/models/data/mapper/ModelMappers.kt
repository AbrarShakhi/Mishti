package com.abrarshakhi.mishti.models.data.mapper

import com.abrarshakhi.mishti.models.data.local.InstalledModelEntity
import com.abrarshakhi.mishti.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.models.domain.model.ShelfModel

fun InstalledModelEntity.toDomain() = ShelfModel(
    id = id,
    name = name,
    origin = runCatching { ModelOrigin.valueOf(origin) }.getOrDefault(ModelOrigin.Imported),
    quantization = quantization,
    parametersLabel = parametersLabel,
    sizeBytes = sizeBytes,
    hfRepo = hfRepo,
    hfFile = hfFile,
    architecture = architecture,
    contextLength = contextLength,
    license = license,
    installedAtMillis = installedAtMillis,
)

fun ShelfModel.toEntity() = InstalledModelEntity(
    id = id,
    name = name,
    origin = origin.name,
    quantization = quantization,
    parametersLabel = parametersLabel,
    sizeBytes = sizeBytes,
    hfRepo = hfRepo,
    hfFile = hfFile,
    architecture = architecture,
    contextLength = contextLength,
    license = license,
    installedAtMillis = installedAtMillis,
)
