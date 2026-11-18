package com.abrarshakhi.mishti.settings.ui.viewmodels.contracts

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.ui.viewmodels.MviViewModel
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState
import com.abrarshakhi.mishti.settings.domain.usecase.ReadDocumentUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DocumentUiState(val isLoading: Boolean = true, val markdown: String = "", val hasFailed: Boolean = false) :
    UiState
