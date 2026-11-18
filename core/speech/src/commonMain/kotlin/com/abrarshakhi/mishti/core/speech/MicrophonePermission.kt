package com.abrarshakhi.mishti.core.speech

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMicrophonePermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
