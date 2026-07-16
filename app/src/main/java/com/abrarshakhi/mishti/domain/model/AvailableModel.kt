package com.abrarshakhi.mishti.domain.model

data class AvailableModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String,
)
