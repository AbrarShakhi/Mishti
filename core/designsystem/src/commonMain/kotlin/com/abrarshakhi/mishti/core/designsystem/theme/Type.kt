package com.abrarshakhi.mishti.core.designsystem.theme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.abrarshakhi.mishti.core.designsystem.resources.edu_nsw_act_cursive
import com.abrarshakhi.mishti.core.designsystem.resources.inter
import com.abrarshakhi.mishti.core.designsystem.resources.jetbrains_mono
import com.abrarshakhi.mishti.core.designsystem.resources.lora
import com.abrarshakhi.mishti.core.domain.theme.AppFont
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes

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
fun codeFontFamily(): FontFamily = variableFamily(CoreDesignsystemRes.font.jetbrains_mono)

@Composable
fun appMarkFontFamily(): FontFamily = variableFamily(CoreDesignsystemRes.font.edu_nsw_act_cursive)

@Composable
fun AppFont.fontFamily(): FontFamily? = when (this) {
    AppFont.System -> null
    AppFont.Inter -> variableFamily(CoreDesignsystemRes.font.inter)
    AppFont.Lora -> variableFamily(CoreDesignsystemRes.font.lora)
    AppFont.JetBrainsMono -> variableFamily(CoreDesignsystemRes.font.jetbrains_mono)
}

fun typographyFor(family: FontFamily?): Typography =
    if (family == null) Typography() else Typography(fontFamily = family)
