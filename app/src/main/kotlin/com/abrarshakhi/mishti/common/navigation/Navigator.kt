package com.abrarshakhi.mishti.common.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator {
    val backStack: List<AppRouteKey>
        field: SnapshotStateList<AppRouteKey> = mutableStateListOf(AppRouteKey.Splash)

    val currentRoute get() = backStack.lastOrNull()

    fun navigate(route: AppRouteKey) {
        backStack.add(route)
    }

    fun resetTo(route: AppRouteKey) {
        backStack.clear()
        navigate(route)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}
