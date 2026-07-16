package com.abrarshakhi.mishti.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis(),
)
