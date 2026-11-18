package com.abrarshakhi.mishti.core.speech.permission

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMicrophonePermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
