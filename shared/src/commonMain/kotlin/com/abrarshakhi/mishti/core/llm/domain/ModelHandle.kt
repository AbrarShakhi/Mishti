package com.abrarshakhi.mishti.core.llm.domain

import kotlinx.coroutines.flow.Flow

data class ModelHandle(val id: String, val name: String, val path: String)

fun interface SelectedModelSource {
    fun selected(): Flow<ModelHandle?>
}
