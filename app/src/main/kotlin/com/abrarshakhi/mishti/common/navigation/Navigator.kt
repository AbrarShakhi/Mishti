package com.abrarshakhi.mishti.common.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(private val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks.getValue(state.topLevelRoute).add(route)
        }
    }

    /**
     * Makes [route] the only entry on the current stack, so back cannot return to what it
     * replaced. For jumps such as opening a conversation or finishing onboarding.
     */
    fun resetTo(route: NavKey) {
        val currentStack = state.backStacks.getValue(state.topLevelRoute)
        currentStack.clear()
        currentStack.add(route)
    }

    fun goBack() {
        val currentStack = state.backStacks.getValue(state.topLevelRoute)
        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}