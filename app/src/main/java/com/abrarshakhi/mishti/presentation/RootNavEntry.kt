package com.abrarshakhi.mishti.presentation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

interface RootNavEntry : RootNavKey, NavEntryProvider

interface RootNavKey : NavKey

interface NavEntryProvider {
    fun route(
        route: NavKey,
        backStack: NavBackStack<NavKey>
    ) : NavEntry<NavKey>
}