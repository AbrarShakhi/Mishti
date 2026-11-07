package com.abrarshakhi.mishti.core.ui.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DrawerController {

    var activeSessionId: String? by mutableStateOf(null)
        private set

    fun onSessionVisible(sessionId: String?) {
        activeSessionId = sessionId
    }

    val state: DrawerState = DrawerState(DrawerValue.Closed)

    suspend fun open() = state.open()

    suspend fun close() = state.close()

    val isOpen: Boolean get() = state.isOpen || state.targetValue == DrawerValue.Open
}
