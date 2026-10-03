package com.abrarshakhi.mishti.common.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    System("System"), Light("Light"), Dark("Dark"),
}

/**
 * The palettes offered in Settings. Every one but [Dynamic] is generated from its [seed] with
 * Material Kolor; [Dynamic] uses the wallpaper colours on Android 12+ and falls back to its seed
 * below that.
 */
enum class AppColorScheme(val label: String, val seed: Color) {
    Dynamic("Dynamic", HoneySeed),
    Honey("Honey", HoneySeed),
    Indigo("Indigo", Color(0xFF4656B5)),
    Forest("Forest", Color(0xFF196C42)),
    Rose("Rose", Color(0xFFA3365E)),
}

enum class AppFont(val label: String) {
    System("System"), Inter("Inter"), Lora("Lora"), JetBrainsMono("JetBrains Mono"),
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    val colorScheme: AppColorScheme = AppColorScheme.Dynamic,
    val font: AppFont = AppFont.System,
)

private val HoneySeed = Color(0xFF865300)
