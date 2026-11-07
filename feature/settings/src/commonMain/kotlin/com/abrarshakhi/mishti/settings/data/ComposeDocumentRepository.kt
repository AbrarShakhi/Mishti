package com.abrarshakhi.mishti.settings.data

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.settings.domain.repository.DocumentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

class ComposeDocumentRepository(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : DocumentRepository {
    override suspend fun read(document: AppDocument): String = withContext(dispatcher) {
        FeatureSettingsRes.readBytes("files/docs/${document.fileName}").decodeToString()
    }
}
