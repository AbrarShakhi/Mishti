package com.abrarshakhi.mishti.core.domain.llm

import kotlinx.coroutines.flow.Flow

data class ModelHandle(val id: String, val name: String, val path: String)

fun interface SelectedModelSource {
    fun selected(): Flow<ModelHandle?>
}
