package com.abrarshakhi.mishti.core.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

@Composable
fun SnackBarProvider(content: @Composable () -> Unit) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarDispatcher: SnackBarDispatcher = koinInject()
    LaunchedEffect(snackBarDispatcher) {
        snackBarDispatcher.messages.collect { message ->
            val resolvedText = message.text.resolve()
            snackBarHostState.showSnackbar(
                message = resolvedText,
                withDismissAction = message.duration != SnackbarDuration.Short,
                duration = message.duration,
            )
        }
    }
    content()
}
