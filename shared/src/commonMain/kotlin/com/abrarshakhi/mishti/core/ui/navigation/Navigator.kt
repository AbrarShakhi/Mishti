package com.abrarshakhi.mishti.core.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator {
    val backStack: List<Screen>
        field: SnapshotStateList<Screen> = mutableStateListOf(Screen.Splash)

    val currentScreen get() = backStack.lastOrNull()

    fun navigate(route: Screen) {
        backStack.add(route)
    }

    fun resetTo(route: Screen) {
        backStack.clear()
        navigate(route)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}
