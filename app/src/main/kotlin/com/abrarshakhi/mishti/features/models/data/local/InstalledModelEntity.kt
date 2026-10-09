package com.abrarshakhi.mishti.features.models.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import kotlinx.coroutines.flow.Flow

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

@Dao
interface InstalledModelDao {
    @Query("SELECT * FROM installed_models ORDER BY installedAtMillis DESC")
    fun observeAll(): Flow<List<InstalledModelEntity>>

    @Query("SELECT * FROM installed_models")
    suspend fun all(): List<InstalledModelEntity>

    @Query("SELECT * FROM installed_models WHERE id = :id")
    suspend fun byId(id: String): InstalledModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: InstalledModelEntity)

    @Query("DELETE FROM installed_models WHERE id = :id")
    suspend fun delete(id: String)
}

fun InstalledModelEntity.toDomain() =
    ShelfModel(
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

fun ShelfModel.toEntity() =
    InstalledModelEntity(
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
