package com.abrarshakhi.mishti.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Dao
interface ModelDao {
    @Query("SELECT * FROM models ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: ModelEntity)

    @Delete
    suspend fun delete(model: ModelEntity)
}

@Database(entities = [ModelEntity::class], version = 1, exportSchema = false)
abstract class ModelDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
}