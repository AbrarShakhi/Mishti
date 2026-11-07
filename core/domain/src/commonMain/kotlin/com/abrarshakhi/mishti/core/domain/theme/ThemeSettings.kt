package com.abrarshakhi.mishti.core.domain.theme

enum class ThemeMode { System, Light, Dark }

enum class AppColorScheme { Dynamic, Honey, Indigo, Forest, Rose }

enum class AppFont { System, Inter, Lora, JetBrainsMono }

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    val colorScheme: AppColorScheme = AppColorScheme.Dynamic,
    val font: AppFont = AppFont.System,
)
