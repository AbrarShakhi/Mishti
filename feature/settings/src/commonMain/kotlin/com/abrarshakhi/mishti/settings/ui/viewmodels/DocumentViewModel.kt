package com.abrarshakhi.mishti.settings.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.ui.viewmodels.MviViewModel
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState
import com.abrarshakhi.mishti.settings.domain.usecase.ReadDocumentUseCase
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.DocumentEffect
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.DocumentIntent
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.DocumentUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DocumentViewModel(private val document: AppDocument, private val readDocument: ReadDocumentUseCase) :
    MviViewModel<DocumentUiState, DocumentIntent, DocumentEffect>(DocumentUiState()) {
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun handleIntent(intent: DocumentIntent) {
        when (intent) {
            DocumentIntent.Retry -> load()
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            updateState { DocumentUiState() }
            try {
                val content = readDocument(document)
                updateState { DocumentUiState(isLoading = false, markdown = content, hasFailed = content.isBlank()) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                updateState { DocumentUiState(isLoading = false, hasFailed = true) }
            }
        }
    }
}
