package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MishtiColor {
    val backgroundBaseLight = Color(0xFFF8F5F0)
    val backgroundBaseDark = Color(0xFF141414)

    val textPrimaryLight = Color(0xFF1A1A1A)
    val textPrimaryDark = Color(0xFFE8E4DF)

    val textMutedLight = Color(0xFF8A8680)
    val textMutedDark = Color(0xFF777777)

    val borderLight = Color(0xFFD4D0C8)
    val borderDark = Color(0xFF2A2A2A)

    val fillFaintLight = Color(0xFFF0EBE4)
    val fillFaintDark = Color(0xFF1E1E1E)

    val accentLight = Color(0xFFFDCE13)
    val accentDark = Color(0xFFFDCE13)

    val actionLight = textPrimaryLight
    val actionDark = accentDark

    val userMessageTextLight = Color(0xFF555555)
    val userMessageTextDark = Color(0xFF999999)

    val error = Color(0xFFFF4444)
    val success = Color(0xFF4CAF50)
    val stopButton = Color(0xFFFF0000)

    val toastBackgroundLight = Color(0xFF1A1A1A)
    val toastBackgroundDark = Color(0xFFF8F5F0)

    val toastTextLight = Color(0xFFF8F5F0)
    val toastTextDark = Color(0xFF1A1A1A)

    @Composable
    fun backgroundBase(): Color = if (isSystemInDarkTheme()) backgroundBaseDark else backgroundBaseLight

    @Composable
    fun textPrimary(): Color = if (isSystemInDarkTheme()) textPrimaryDark else textPrimaryLight

    @Composable
    fun textMuted(): Color = if (isSystemInDarkTheme()) textMutedDark else textMutedLight

    @Composable
    fun border(): Color = if (isSystemInDarkTheme()) borderDark else borderLight

    @Composable
    fun fillFaint(): Color = if (isSystemInDarkTheme()) fillFaintDark else fillFaintLight

    @Composable
    fun accent(): Color = if (isSystemInDarkTheme()) accentDark else accentLight

    @Composable
    fun action(): Color = if (isSystemInDarkTheme()) actionDark else actionLight

    @Composable
    fun userMessageText(): Color = if (isSystemInDarkTheme()) userMessageTextDark else userMessageTextLight

    @Composable
    fun toastBackground(): Color = if (isSystemInDarkTheme()) toastBackgroundDark else toastBackgroundLight

    @Composable
    fun toastText(): Color = if (isSystemInDarkTheme()) toastTextDark else toastTextLight
}