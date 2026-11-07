package com.abrarshakhi.mishti.settings.domain.repository

import com.abrarshakhi.mishti.settings.domain.model.AppDocument

fun interface DocumentRepository {
    suspend fun read(document: AppDocument): String
}
