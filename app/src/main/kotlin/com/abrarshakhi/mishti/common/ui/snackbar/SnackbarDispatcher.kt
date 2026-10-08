package com.abrarshakhi.mishti.common.ui.snackbar

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import com.abrarshakhi.mishti.common.ui.text.UiText
import com.abrarshakhi.mishti.common.ui.text.uiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarMessage(
    val text: UiText,
    val duration: SnackbarDuration = SnackbarDuration.Short,
)

class SnackbarDispatcher {

    private val _messages = Channel<SnackbarMessage>(Channel.BUFFERED)
    val messages: Flow<SnackbarMessage> = _messages.receiveAsFlow()

    fun show(text: UiText, duration: SnackbarDuration = SnackbarDuration.Short) {
        _messages.trySend(SnackbarMessage(text, duration))
    }

    fun show(@StringRes id: Int, vararg args: Any) = show(uiText(id, *args))

    fun showError(text: UiText) = show(text, SnackbarDuration.Long)

    fun showError(@StringRes id: Int, vararg args: Any) = showError(uiText(id, *args))
}
