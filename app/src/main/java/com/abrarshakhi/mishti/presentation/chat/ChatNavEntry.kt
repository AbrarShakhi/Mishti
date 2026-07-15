package com.abrarshakhi.mishti.presentation.chat

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.mishti.presentation.RootNavEntry
import kotlinx.serialization.Serializable

@Serializable
object ChatNavEntry : RootNavEntry {
    override fun route(route: NavKey, backStack: NavBackStack<NavKey>): NavEntry<NavKey> {
        return NavEntry(route) {
            ChatRoute(
                onOpenModels = {},
                onOpenSettings = {}
            )
        }
    }
}


