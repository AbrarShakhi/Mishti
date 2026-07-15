package com.abrarshakhi.mishti.presentation.chat

sealed interface ChatIntent {
    object NewChat : ChatIntent
}
