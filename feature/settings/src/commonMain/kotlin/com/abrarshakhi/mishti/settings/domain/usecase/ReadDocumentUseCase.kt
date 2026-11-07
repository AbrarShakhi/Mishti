package com.abrarshakhi.mishti.settings.domain.usecase

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.settings.domain.repository.DocumentRepository

class ReadDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(document: AppDocument): String =
        unindentWrappedLines(stripLeadingTitle(repository.read(document)))
}
