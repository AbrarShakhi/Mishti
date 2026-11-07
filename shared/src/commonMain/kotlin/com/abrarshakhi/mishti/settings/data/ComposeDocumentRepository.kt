package com.abrarshakhi.mishti.settings.data

import com.abrarshakhi.mishti.settings.domain.model.AppDocument
import com.abrarshakhi.mishti.settings.domain.repository.DocumentRepository
import com.abrarshakhi.mishti.shared.resources.Res
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ComposeDocumentRepository(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : DocumentRepository {
    override suspend fun read(document: AppDocument): String = withContext(dispatcher) {
        Res.readBytes("files/docs/${document.fileName}").decodeToString()
    }
}
