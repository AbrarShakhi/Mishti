package com.abrarshakhi.mishti.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.edu_nsw_act_cursive
import com.abrarshakhi.mishti.shared.resources.inter
import com.abrarshakhi.mishti.shared.resources.jetbrains_mono
import com.abrarshakhi.mishti.shared.resources.lora
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

private val VariableWeights = listOf(
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
)

@Composable
private fun variableFamily(resource: FontResource): FontFamily {
    val fonts = VariableWeights.map { weight ->
        Font(
            resource = resource,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    }
    return remember(fonts) { FontFamily(fonts) }
}

@Composable
fun codeFontFamily(): FontFamily = variableFamily(Res.font.jetbrains_mono)

@Composable
fun appMarkFontFamily(): FontFamily = variableFamily(Res.font.edu_nsw_act_cursive)

@Composable
fun AppFont.fontFamily(): FontFamily? = when (this) {
    AppFont.System -> null
    AppFont.Inter -> variableFamily(Res.font.inter)
    AppFont.Lora -> variableFamily(Res.font.lora)
    AppFont.JetBrainsMono -> variableFamily(Res.font.jetbrains_mono)
}

fun typographyFor(family: FontFamily?): Typography =
    if (family == null) Typography() else Typography(fontFamily = family)
