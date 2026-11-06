package com.abrarshakhi.mishti.core.ui.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue

class DrawerController {

    val state: DrawerState = DrawerState(DrawerValue.Closed)

    suspend fun open() = state.open()

    suspend fun close() = state.close()

    val isOpen: Boolean get() = state.isOpen
}
