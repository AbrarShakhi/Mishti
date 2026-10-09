package com.abrarshakhi.mishti.common.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(startRoute: AppRouteKey) {
    val backStack: List<AppRouteKey>
        field: SnapshotStateList<AppRouteKey> = mutableStateListOf(startRoute)

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
