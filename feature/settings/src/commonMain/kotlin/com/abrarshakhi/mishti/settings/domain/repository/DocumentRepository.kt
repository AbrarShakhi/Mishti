package com.abrarshakhi.mishti.settings.domain.repository

import com.abrarshakhi.mishti.core.domain.documents.AppDocument

fun interface DocumentRepository {
    suspend fun read(document: AppDocument): String
}
