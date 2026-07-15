package com.abrarshakhi.mishti.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.data.repository.ModelRepository
import com.abrarshakhi.mishti.llm.LlamaEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ChatViewModel(
    private val engine: LlamaEngine,
    private val repository: ModelRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _state.onStart { }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5001L),
        initialValue = ChatUiState()
    )

    fun onIntent(intent: ChatIntent) {
    }
}