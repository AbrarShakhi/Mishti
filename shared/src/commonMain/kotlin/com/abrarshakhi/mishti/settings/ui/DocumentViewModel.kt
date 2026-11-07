package com.abrarshakhi.mishti.settings.ui

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.ui.mvi.MviViewModel
import com.abrarshakhi.mishti.core.ui.mvi.UiEffect
import com.abrarshakhi.mishti.core.ui.mvi.UiIntent
import com.abrarshakhi.mishti.core.ui.mvi.UiState
import com.abrarshakhi.mishti.settings.domain.model.AppDocument
import com.abrarshakhi.mishti.settings.domain.usecase.ReadDocumentUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DocumentUiState(val isLoading: Boolean = true, val markdown: String = "", val hasFailed: Boolean = false) :
    UiState
sealed interface DocumentIntent : UiIntent {
    data object Retry : DocumentIntent
}
sealed interface DocumentEffect : UiEffect

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
