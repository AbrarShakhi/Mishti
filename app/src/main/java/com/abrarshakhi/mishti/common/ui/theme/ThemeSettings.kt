package com.abrarshakhi.mishti.common.ui.theme

enum class ThemeMode(val label: String) {
    System("System"), Light("Light"), Dark("Dark"),
}

enum class AppColorScheme(val label: String) {
    Dynamic("Dynamic"), Honey("Honey"), Indigo("Indigo"), Forest("Forest"), Rose("Rose"),
}

enum class AppFont(val label: String) {
    System("System"), Inter("Inter"), Lora("Lora"), JetBrainsMono("JetBrains Mono"),
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    val colorScheme: AppColorScheme = AppColorScheme.Dynamic,
    val font: AppFont = AppFont.System,
)
