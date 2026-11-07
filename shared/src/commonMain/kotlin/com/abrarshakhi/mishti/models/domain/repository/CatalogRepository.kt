package com.abrarshakhi.mishti.models.domain.repository

import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.CatalogState
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun state(source: CatalogSource): Flow<CatalogState>

    suspend fun refresh(source: CatalogSource, force: Boolean)
}
