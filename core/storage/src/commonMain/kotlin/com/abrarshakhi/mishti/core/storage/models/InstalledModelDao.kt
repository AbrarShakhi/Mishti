package com.abrarshakhi.mishti.core.storage.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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
