package com.abrarshakhi.mishti.features.chat.presentation

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.mvi.MviViewModel
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SessionsViewModel(
    private val repository: ChatRepository,
    private val snackbar: SnackbarDispatcher,
) : MviViewModel<SessionsUiState, SessionsIntent, SessionsEffect>(SessionsUiState()) {

    init {
        viewModelScope.launch {
            repository.observeSessions().collect { sessions ->
                updateState {
                    copy(
                        sessions = sessions,
                        actionsFor = actionsFor?.let { open -> sessions.find { it.id == open.id } },
                        deleting = deleting?.let { open -> sessions.find { it.id == open.id } },
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: SessionsIntent) {
        when (intent) {
            SessionsIntent.NewChatClicked -> onNewChatClicked()

            is SessionsIntent.SessionSelected -> emitEffect(SessionsEffect.OpenSession(intent.sessionId))

            is SessionsIntent.SessionLongPressed -> updateState {
                copy(actionsFor = sessions.find { it.id == intent.sessionId })
            }

            SessionsIntent.ActionsDismissed -> updateState { copy(actionsFor = null) }

            SessionsIntent.RenameRequested -> updateState {
                val target = actionsFor
                copy(
                    actionsFor = null,
                    renaming = target?.let { RenameState(it.id, it.title) },
                )
            }

            is SessionsIntent.RenameTitleChanged -> updateState {
                copy(renaming = renaming?.copy(title = intent.title))
            }

            SessionsIntent.RenameConfirmed -> onRenameConfirmed()

            SessionsIntent.RenameCancelled -> updateState { copy(renaming = null) }

            SessionsIntent.DeleteRequested -> updateState {
                copy(actionsFor = null, deleting = actionsFor)
            }

            is SessionsIntent.DeleteConfirmed -> onDeleteConfirmed(intent.visibleSessionId)

            SessionsIntent.DeleteCancelled -> updateState { copy(deleting = null) }
        }
    }

    private fun onNewChatClicked() {
        viewModelScope.launch {
            val sessionId = try {
                emptySessionId()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackbar.showError("Could not start a new chat.")
                return@launch
            }
            emitEffect(SessionsEffect.OpenSession(sessionId))
        }
    }

    private fun onRenameConfirmed() {
        val renaming = currentState.renaming ?: return
        if (!renaming.canConfirm) return

        updateState { copy(renaming = null) }

        viewModelScope.launch {
            runCatching {
                repository.renameSession(
                    renaming.sessionId,
                    renaming.title.trim()
                )
            }.onFailure { snackbar.showError("Could not rename that conversation.") }
        }
    }

    private fun onDeleteConfirmed(visibleSessionId: String?) {
        val target = currentState.deleting ?: return
        updateState { copy(deleting = null) }

        viewModelScope.launch {
            val deleted = runCatching { repository.deleteSession(target.id) }.isSuccess
            if (!deleted) {
                snackbar.showError("Could not delete that conversation.")
                return@launch
            }
            snackbar.show("Conversation deleted.")

            if (target.id != visibleSessionId) return@launch

            val replacement = try {
                emptySessionId()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@launch
            }
            emitEffect(SessionsEffect.OpenSession(replacement))
        }
    }

    private suspend fun emptySessionId(): String {
        val latest = repository.latestSessionId()
        return if (latest != null && repository.isSessionEmpty(latest)) {
            latest
        } else {
            repository.createSession(UNTITLED_SESSION)
        }
    }
}
