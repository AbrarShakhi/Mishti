package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MishtiColor {
    // ── Backgrounds ──────────────────────────────────────────────────────────
    val backgroundBaseLight = Color(0xFFFAF9F5)
    val backgroundBaseDark  = Color(0xFF24252A)

    // ── Surfaces ──────────────────────────────────────────────────────────────
    val surfaceLight        = Color(0xFFFFFFFF)
    val surfaceDark         = Color(0xFF2E3036)

    val surfaceMutedLight   = Color(0xFFEFEFEA)
    val surfaceMutedDark    = Color(0xFF383A41)

    // ── Borders ───────────────────────────────────────────────────────────────
    val borderLight         = Color(0xFFE3E2DC)
    val borderDark          = Color(0xFF44464E)

    // ── Text ──────────────────────────────────────────────────────────────────
    val textPrimaryLight    = Color(0xFF1A1915)
    val textPrimaryDark     = Color(0xFFF3F3F0)

    val textSecondaryLight  = Color(0xFF6F6E66)
    val textSecondaryDark   = Color(0xFFB4B5B0)

    val textTertiaryLight   = Color(0xFF9A9890)
    val textTertiaryDark    = Color(0xFF84858A)

    // ── Accent ────────────────────────────────────────────────────────────────
    val accentLight         = Color(0xFF2D63C8)
    val accentDark          = Color(0xFF6B9BF2)

    val accentHoverLight    = Color(0xFF234FA3)
    val accentHoverDark     = Color(0xFF87AEF7)

    val onAccentLight       = Color(0xFFFAFBFF)
    val onAccentDark        = Color(0xFF15171C)

    // ── Semantic (theme-independent) ──────────────────────────────────────────
    val error               = Color(0xFFFF4444)
    val success             = Color(0xFF4CAF50)
    val stopButton          = Color(0xFFFF0000)

    // ── Toast ─────────────────────────────────────────────────────────────────
    val toastBackgroundLight = Color(0xFF1A1915)
    val toastBackgroundDark  = Color(0xFFF3F3F0)

    val toastTextLight       = Color(0xFFF3F3F0)
    val toastTextDark        = Color(0xFF1A1915)

    // ── Composable helpers ────────────────────────────────────────────────────

    @Composable
    fun backgroundBase(): Color = if (isSystemInDarkTheme()) backgroundBaseDark else backgroundBaseLight

    @Composable
    fun surface(): Color = if (isSystemInDarkTheme()) surfaceDark else surfaceLight

    @Composable
    fun surfaceMuted(): Color = if (isSystemInDarkTheme()) surfaceMutedDark else surfaceMutedLight

    @Composable
    fun border(): Color = if (isSystemInDarkTheme()) borderDark else borderLight

    @Composable
    fun textPrimary(): Color = if (isSystemInDarkTheme()) textPrimaryDark else textPrimaryLight

    @Composable
    fun textSecondary(): Color = if (isSystemInDarkTheme()) textSecondaryDark else textSecondaryLight

    @Composable
    fun textTertiary(): Color = if (isSystemInDarkTheme()) textTertiaryDark else textTertiaryLight

    @Composable
    fun accent(): Color = if (isSystemInDarkTheme()) accentDark else accentLight

    @Composable
    fun accentHover(): Color = if (isSystemInDarkTheme()) accentHoverDark else accentHoverLight

    @Composable
    fun onAccent(): Color = if (isSystemInDarkTheme()) onAccentDark else onAccentLight

    @Composable
    fun toastBackground(): Color = if (isSystemInDarkTheme()) toastBackgroundDark else toastBackgroundLight

    @Composable
    fun toastText(): Color = if (isSystemInDarkTheme()) toastTextDark else toastTextLight

    // ── Legacy aliases (kept so existing call-sites don't break) ─────────────
    // These point to the closest semantic equivalent in the new palette.
    // Replace usages gradually with the new names above.

    @Composable
    fun fillFaint(): Color = surfaceMuted()          // was fillFaint → now surfaceMuted

    @Composable
    fun textMuted(): Color = textSecondary()         // was textMuted  → now textSecondary

    @Composable
    fun action(): Color = accent()                   // was action     → now accent
}