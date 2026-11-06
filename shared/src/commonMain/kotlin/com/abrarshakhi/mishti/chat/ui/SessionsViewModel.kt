package com.abrarshakhi.mishti.chat.ui

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.chat.domain.repository.ChatRepository
import com.abrarshakhi.mishti.chat.domain.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.core.ui.mvi.MviViewModel
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.chat_deleted
import com.abrarshakhi.mishti.shared.resources.chat_error_delete
import com.abrarshakhi.mishti.shared.resources.chat_error_new
import com.abrarshakhi.mishti.shared.resources.chat_error_rename
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SessionsViewModel(
    private val repository: ChatRepository,
    private val createNewSession: CreateNewSessionUseCase,
    private val snackBar: SnackBarDispatcher,
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

            is SessionsIntent.SessionSelected -> {
                emitEffect(SessionsEffect.OpenSession(intent.sessionId))
            }

            is SessionsIntent.SessionLongPressed -> {
                updateState {
                    copy(actionsFor = sessions.find { it.id == intent.sessionId })
                }
            }

            SessionsIntent.ActionsDismissed -> {
                updateState { copy(actionsFor = null) }
            }

            SessionsIntent.RenameRequested -> {
                updateState {
                    val target = actionsFor
                    copy(
                        actionsFor = null,
                        renaming = target?.let { RenameState(it.id, it.title) },
                    )
                }
            }

            is SessionsIntent.RenameTitleChanged -> {
                updateState {
                    copy(renaming = renaming?.copy(title = intent.title))
                }
            }

            SessionsIntent.RenameConfirmed -> onRenameConfirmed()

            SessionsIntent.RenameCancelled -> {
                updateState { copy(renaming = null) }
            }

            SessionsIntent.DeleteRequested -> {
                updateState {
                    copy(actionsFor = null, deleting = actionsFor)
                }
            }

            is SessionsIntent.DeleteConfirmed -> {
                onDeleteConfirmed(intent.visibleSessionId)
            }

            SessionsIntent.DeleteCancelled -> {
                updateState { copy(deleting = null) }
            }
        }
    }

    private fun onNewChatClicked() {
        viewModelScope.launch {
            val sessionId = try {
                createNewSession()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                snackBar.showError(Res.string.chat_error_new)
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
                    renaming.title.trim(),
                )
            }.onFailure { snackBar.showError(Res.string.chat_error_rename) }
        }
    }

    private fun onDeleteConfirmed(visibleSessionId: String?) {
        val target = currentState.deleting ?: return
        updateState { copy(deleting = null) }

        viewModelScope.launch {
            val deleted = runCatching { repository.deleteSession(target.id) }.isSuccess
            if (!deleted) {
                snackBar.showError(Res.string.chat_error_delete)
                return@launch
            }
            snackBar.show(Res.string.chat_deleted)

            if (target.id != visibleSessionId) return@launch

            val replacement = try {
                createNewSession()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@launch
            }
            emitEffect(SessionsEffect.OpenSession(replacement))
        }
    }
}
