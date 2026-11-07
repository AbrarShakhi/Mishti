package com.abrarshakhi.mishti.models.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_models")
data class InstalledModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val origin: String,
    val quantization: String?,
    val parametersLabel: String?,
    val sizeBytes: Long,
    val hfRepo: String?,
    val hfFile: String?,
    val architecture: String?,
    val contextLength: Int?,
    val license: String?,
    val installedAtMillis: Long,
)
