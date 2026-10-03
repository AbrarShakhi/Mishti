package com.abrarshakhi.mishti.common.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey


private data class TabSpec(
    val route: NavKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val Tabs = emptyList<TabSpec>()

val TOP_LEVEL_ROUTES = Tabs.map { it.route }.toSet()

@Suppress("UNUSED")
val NavKey.isTopLevel: Boolean get() = this in TOP_LEVEL_ROUTES