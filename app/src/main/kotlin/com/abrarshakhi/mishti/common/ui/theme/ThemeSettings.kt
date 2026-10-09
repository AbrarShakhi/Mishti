package com.abrarshakhi.mishti.common.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeMode { System, Light, Dark }

enum class AppColorScheme(
    val seed: Color,
) {
    Dynamic(HoneySeed),
    Honey(HoneySeed),
    Indigo(Color(0xFF4656B5)),
    Forest(Color(0xFF196C42)),
    Rose(Color(0xFFA3365E)),
}

enum class AppFont { System, Inter, Lora, JetBrainsMono }

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    val colorScheme: AppColorScheme = AppColorScheme.Dynamic,
    val font: AppFont = AppFont.System,
)

private val HoneySeed = Color(0xFF865300)
