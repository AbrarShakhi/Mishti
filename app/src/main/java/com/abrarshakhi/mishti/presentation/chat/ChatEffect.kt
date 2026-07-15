package com.abrarshakhi.mishti.presentation.chat

sealed interface ChatEffect {
    data class Error(val message: String) : ChatEffect
}