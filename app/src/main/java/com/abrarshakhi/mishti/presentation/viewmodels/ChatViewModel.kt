package com.abrarshakhi.mishti.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.data.repository.ModelRepository
import com.abrarshakhi.mishti.llm.LlamaEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Data types
// ─────────────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val role: Role,
    val text: String
) {
    enum class Role { USER, ASSISTANT }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val isModelLoaded: Boolean = false,
    val loadingError: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ChatViewModel
// ─────────────────────────────────────────────────────────────────────────────
class ChatViewModel(
    private val engine: LlamaEngine,
    private val repository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    // ── Load the model ────────────────────────────────────────────────────────
    fun loadModel(modelId: String) {
        viewModelScope.launch {
            val path = repository.getFilePath(modelId)
            if (path == null) {
                _uiState.update { it.copy(loadingError = "Model file not found") }
                return@launch
            }

            val ok = engine.loadModel(path)
            _uiState.update {
                it.copy(
                    isModelLoaded = ok,
                    loadingError  = if (ok) null else "Failed to load model"
                )
            }
        }
    }

    // ── Send a message ────────────────────────────────────────────────────────
    fun sendMessage(userText: String) {
        if (userText.isBlank() || _uiState.value.isGenerating) return

        // 1. Add the user's message to the list immediately
        val userMsg = ChatMessage(ChatMessage.Role.USER, userText.trim())
        _uiState.update { state ->
            state.copy(
                messages     = state.messages + userMsg,
                isGenerating = true
            )
        }

        // 2. Add an empty assistant message that we'll fill in token by token
        val assistantMsgIndex = _uiState.value.messages.size
        _uiState.update { state ->
            state.copy(messages = state.messages + ChatMessage(ChatMessage.Role.ASSISTANT, ""))
        }

        // 3. Build the prompt in ChatML format (what most instruction-tuned models expect)
        val prompt = buildChatMLPrompt(_uiState.value.messages.dropLast(1))

        // 4. Stream tokens from the engine
        generationJob = viewModelScope.launch {
            engine.complete(prompt).collect { token ->
                _uiState.update { state ->
                    val updated = state.messages.toMutableList()
                    val current = updated[assistantMsgIndex]
                    updated[assistantMsgIndex] = current.copy(text = current.text + token)
                    state.copy(messages = updated)
                }
            }

            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    /** Stop generating mid-stream. */
    fun stopGeneration() {
        generationJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }

    /** Wipe the conversation history. */
    fun clearChat() {
        stopGeneration()
        _uiState.update { it.copy(messages = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        // Release native memory when the ViewModel is destroyed
        viewModelScope.launch { engine.reset() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prompt formatting
    //
    // ChatML is the format most small models (TinyLlama, Phi-3, Gemma) expect:
    //
    //   <|system|>You are a helpful assistant.<|end|>
    //   <|user|>Hello<|end|>
    //   <|assistant|>
    //
    // The model then generates text until it outputs <|end|> or EOS.
    // ─────────────────────────────────────────────────────────────────────────
    private fun buildChatMLPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("<|system|>You are Mishti, a helpful AI assistant running fully offline on this device. Be concise and friendly.<|end|>\n")
        for (msg in messages) {
            val role = if (msg.role == ChatMessage.Role.USER) "user" else "assistant"
            sb.append("<|${role}|>${msg.text}<|end|>\n")
        }
        sb.append("<|assistant|>")  // model fills in from here
        return sb.toString()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory — lets us pass constructor args to the ViewModel
    // (Android normally only allows no-arg ViewModels)
    // ─────────────────────────────────────────────────────────────────────────
    class Factory(
        private val engine: LlamaEngine,
        private val repository: ModelRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(engine, repository) as T
    }
}