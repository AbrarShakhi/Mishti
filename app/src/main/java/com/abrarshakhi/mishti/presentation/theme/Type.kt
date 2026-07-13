package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abrarshakhi.mishti.R

object MishtiTypography {
    private val serifFamily = FontFamily(
        Font(R.font.fraunces, FontWeight.Medium),
        Font(R.font.fraunces_bold, FontWeight.SemiBold)
    )

    private val uiFamily = FontFamily(
        Font(R.font.fraunces, FontWeight.Normal),
        Font(R.font.fraunces, FontWeight.Medium),
        Font(R.font.fraunces_bold, FontWeight.SemiBold),
        Font(R.font.fraunces_bold, FontWeight.Bold)
    )

    private val messageFamily = uiFamily

    private val codeFamily = FontFamily(
        Font(R.font.jetbrains_mono, FontWeight.Normal)
    )

    val h1 = TextStyle(fontFamily = serifFamily, fontSize = 48.sp, fontWeight = FontWeight.Medium, lineHeight = 82.sp)
    val h2 = TextStyle(fontFamily = serifFamily, fontSize = 32.sp, fontWeight = FontWeight.Medium, lineHeight = 39.sp)
    val h3 = TextStyle(fontFamily = serifFamily, fontSize = 24.sp, fontWeight = FontWeight.Medium, lineHeight = 29.sp)
    val large = TextStyle(fontFamily = serifFamily, fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)

    val h1Bold = TextStyle(fontFamily = serifFamily, fontSize = 48.sp, fontWeight = FontWeight.SemiBold, lineHeight = 82.sp)
    val h2Bold = TextStyle(fontFamily = serifFamily, fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 39.sp)
    val h3Bold = TextStyle(fontFamily = serifFamily, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 29.sp)

    val body = TextStyle(fontFamily = uiFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    val small = TextStyle(fontFamily = uiFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp)
    val mini = TextStyle(fontFamily = uiFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 15.sp)
    val tiny = TextStyle(fontFamily = uiFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, lineHeight = 12.sp)

    val message = TextStyle(fontFamily = messageFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp)
    val code = TextStyle(fontFamily = codeFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp)

    val material = Typography(
        bodyLarge = body,
        bodyMedium = body,
        bodySmall = small,
        titleLarge = h2,
        titleMedium = h3,
        titleSmall = large,
        labelLarge = small,
        labelMedium = mini,
        labelSmall = tiny
    )
}