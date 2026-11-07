package com.abrarshakhi.mishti.core.ui.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SnackBarProvider(snackBarDispatcher: SnackBarDispatcher, content: @Composable () -> Unit) {
    val snackBarHostState = remember { SnackbarHostState() }
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
    Box(Modifier.fillMaxSize()) {
        content()
        SnackbarHost(snackBarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}
