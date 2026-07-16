package com.abrarshakhi.mishti.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.domain.model.ChatMessage
import com.abrarshakhi.mishti.domain.use_case.CreateConversationUseCase
import com.abrarshakhi.mishti.domain.use_case.GetConversationGroupsUseCase
import com.abrarshakhi.mishti.domain.use_case.LoadModelUseCase
import com.abrarshakhi.mishti.domain.use_case.SendMessageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getConversationGroups: GetConversationGroupsUseCase,
    private val createConversation: CreateConversationUseCase,
    private val sendMessage: SendMessageUseCase,
    private val loadModel: LoadModelUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatUiState(),
        )

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect = _effect.asSharedFlow()

    private var generationJob: Job? = null

    init {
        observeConversationGroups()
        loadDefaultModel()
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.NewChat             -> onNewChat()
            is ChatIntent.SelectConversation  -> onSelectConversation(intent.conversationId)
            is ChatIntent.InputChanged        -> onInputChanged(intent.text)
            is ChatIntent.SendMessage         -> onSendMessage()
        }
    }

    private fun observeConversationGroups() {
        getConversationGroups()
            .onEach { groups ->
                _state.update { it.copy(conversationGroups = groups) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadDefaultModel() {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelLoadError = null) }
            loadModel(_state.value.activeModelId)
                .onFailure { error ->
                    _state.update { it.copy(modelLoadError = error.message) }
                    _effect.emit(ChatEffect.Error("Failed to load model: ${error.message}"))
                }
            _state.update { it.copy(isModelLoading = false) }
        }
    }

    private fun onNewChat() {
        generationJob?.cancel()

        viewModelScope.launch {
            val newId = createConversation(_state.value.activeModelId)
            _state.update { it.copy(activeConversationId = newId, messages = emptyList()) }
        }
    }

    private fun onSelectConversation(conversationId: String) {
        generationJob?.cancel()
        // Messages for the selected conversation are loaded separately.
        // For now we just switch the active id; the message-observing
        // logic will be wired here in the next step (message list screen).
        _state.update { it.copy(activeConversationId = conversationId) }
    }

    private fun onInputChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    private fun onSendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isGenerating) return

        val conversationId = _state.value.activeConversationId ?: run {
            viewModelScope.launch {
                val newId = createConversation(_state.value.activeModelId)
                _state.update { it.copy(activeConversationId = newId) }
                startGeneration(newId, text)
            }
            return
        }

        startGeneration(conversationId, text)
    }

    private fun startGeneration(conversationId: String, userText: String) {
        _state.update { state ->
            state.copy(
                inputText = "",
                isGenerating = true,
                messages = state.messages + ChatMessage(ChatMessage.Role.USER, userText),
            )
        }

        var streamedReply = ""

        generationJob = viewModelScope.launch {
            sendMessage(conversationId, userText)
                .collect { token ->
                    streamedReply += token
                    _state.update { state ->
                        val updatedMessages = if (state.messages.lastOrNull()?.role == ChatMessage.Role.LLM) {
                            state.messages.dropLast(1) + ChatMessage(ChatMessage.Role.LLM, streamedReply)
                        } else {
                            state.messages + ChatMessage(ChatMessage.Role.LLM, streamedReply)
                        }
                        state.copy(messages = updatedMessages)
                    }
                }
            _state.update { it.copy(isGenerating = false) }
        }

        generationJob?.invokeOnCompletion { cause ->
            if (cause != null) {
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    class Factory(
        private val getConversationGroups: GetConversationGroupsUseCase,
        private val createConversation: CreateConversationUseCase,
        private val sendMessage: SendMessageUseCase,
        private val loadModel: LoadModelUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(
                getConversationGroups,
                createConversation,
                sendMessage,
                loadModel,
            ) as T
    }
}
