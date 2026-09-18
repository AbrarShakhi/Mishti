package com.abrarshakhi.mishti.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.abrarshakhi.mishti.R

private fun variableFamily(resId: Int): FontFamily = FontFamily(
    listOf(
        FontWeight.Light,
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
    ).map { weight ->
        Font(
            resId = resId,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    })

private val InterFamily by lazy { variableFamily(R.font.inter) }
private val LoraFamily by lazy { variableFamily(R.font.lora) }
private val JetBrainsMonoFamily by lazy { variableFamily(R.font.jetbrains_mono) }

fun AppFont.fontFamily(): FontFamily? = when (this) {
    AppFont.System -> null
    AppFont.Inter -> InterFamily
    AppFont.Lora -> LoraFamily
    AppFont.JetBrainsMono -> JetBrainsMonoFamily
}

fun typographyFor(family: FontFamily?): Typography {
    val base = Typography()
    if (family == null) return base
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}
