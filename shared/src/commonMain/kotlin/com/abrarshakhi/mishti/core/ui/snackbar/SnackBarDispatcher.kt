package com.abrarshakhi.mishti.core.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import com.abrarshakhi.mishti.core.ui.utils.UiText
import com.abrarshakhi.mishti.core.ui.utils.uiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.StringResource

data class SnackBarMessage(val text: UiText, val duration: SnackbarDuration = SnackbarDuration.Short)

class SnackBarDispatcher {
    private val _messages = Channel<SnackBarMessage>(Channel.BUFFERED)
    val messages: Flow<SnackBarMessage> = _messages.receiveAsFlow()

    fun show(text: UiText, duration: SnackbarDuration = SnackbarDuration.Short) {
        _messages.trySend(SnackBarMessage(text, duration))
    }

    fun show(resource: StringResource, vararg args: Any) = show(uiText(resource, *args))

    fun showError(text: UiText) = show(text, SnackbarDuration.Long)

    fun showError(resource: StringResource, vararg args: Any) = showError(uiText(resource, *args))
}
