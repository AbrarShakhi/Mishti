package com.abrarshakhi.mishti.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.abrarshakhi.mishti.R

private fun variableFamily(resId: Int): FontFamily =
    FontFamily(
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
        },
    )

private val InterFamily by lazy { variableFamily(R.font.inter) }
private val LoraFamily by lazy { variableFamily(R.font.lora) }
private val JetBrainsMonoFamily by lazy { variableFamily(R.font.jetbrains_mono) }

val CodeFontFamily: FontFamily get() = JetBrainsMonoFamily

fun AppFont.fontFamily(): FontFamily? =
    when (this) {
        AppFont.System -> null
        AppFont.Inter -> InterFamily
        AppFont.Lora -> LoraFamily
        AppFont.JetBrainsMono -> JetBrainsMonoFamily
    }

fun typographyFor(family: FontFamily?): Typography =
    if (family == null) Typography() else Typography(fontFamily = family)
