package com.abrarshakhi.mishti.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abrarshakhi.mishti.R

// Mirrors the Flutter theme:
//   _serif  → Fraunces  (display / headline sizes)
//   _sans   → Inter     (title / body / label sizes)
//   _code   → JetBrains Mono

object MishtiTypography {

    // ── Font families ─────────────────────────────────────────────────────────

    private val serifFamily = FontFamily(
        Font(R.font.fraunces, FontWeight.SemiBold),  // w600
        Font(R.font.fraunces, FontWeight.Medium)     // w500 fallback
    )

    private val sansFamily = FontFamily(
        Font(R.font.inter, FontWeight.Normal),       // w400
        Font(R.font.inter_medium, FontWeight.Medium),// w500
        Font(R.font.inter_semibold, FontWeight.SemiBold) // w600
    )

    private val codeFamily = FontFamily(
        Font(R.font.jetbrains_mono, FontWeight.Normal)
    )

    // ── Display (Fraunces / serif) ────────────────────────────────────────────

    /** 56sp · w600 · letterSpacing −1.2 · lineHeight 1.05 */
    val displayLarge = TextStyle(
        fontFamily   = serifFamily,
        fontSize     = 56.sp,
        fontWeight   = FontWeight.SemiBold,
        lineHeight   = (56 * 1.05).sp,
        letterSpacing = (-1.2).sp
    )

    /** 42sp · w600 · letterSpacing −0.8 · lineHeight 1.08 */
    val displayMedium = TextStyle(
        fontFamily   = serifFamily,
        fontSize     = 42.sp,
        fontWeight   = FontWeight.SemiBold,
        lineHeight   = (42 * 1.08).sp,
        letterSpacing = (-0.8).sp
    )

    /** 32sp · w600 · letterSpacing −0.5 · lineHeight 1.15 */
    val displaySmall = TextStyle(
        fontFamily   = serifFamily,
        fontSize     = 32.sp,
        fontWeight   = FontWeight.SemiBold,
        lineHeight   = (32 * 1.15).sp,
        letterSpacing = (-0.5).sp
    )

    /** 24sp · w600 · lineHeight 1.2 */
    val headlineMedium = TextStyle(
        fontFamily = serifFamily,
        fontSize   = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (24 * 1.2).sp
    )

    // ── Title / UI (Inter / sans) ─────────────────────────────────────────────

    /** 20sp · w600 · letterSpacing −0.2 · lineHeight 1.3 */
    val titleLarge = TextStyle(
        fontFamily    = sansFamily,
        fontSize      = 20.sp,
        fontWeight    = FontWeight.SemiBold,
        lineHeight    = (20 * 1.3).sp,
        letterSpacing = (-0.2).sp
    )

    /** 16sp · w600 · lineHeight 1.4 */
    val titleMedium = TextStyle(
        fontFamily = sansFamily,
        fontSize   = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (16 * 1.4).sp
    )

    // ── Body (Inter / sans) ───────────────────────────────────────────────────

    /** 18sp · w400 · lineHeight 1.6 */
    val bodyLarge = TextStyle(
        fontFamily = sansFamily,
        fontSize   = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (18 * 1.6).sp
    )

    /** 15sp · w400 · lineHeight 1.6 */
    val bodyMedium = TextStyle(
        fontFamily = sansFamily,
        fontSize   = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (15 * 1.6).sp
    )

    /** 13sp · w400 · lineHeight 1.5 */
    val bodySmall = TextStyle(
        fontFamily = sansFamily,
        fontSize   = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (13 * 1.5).sp
    )

    // ── Label (Inter / sans) ──────────────────────────────────────────────────

    /** 14sp · w600 · letterSpacing 0.1 · lineHeight 1.2 */
    val labelLarge = TextStyle(
        fontFamily    = sansFamily,
        fontSize      = 14.sp,
        fontWeight    = FontWeight.SemiBold,
        lineHeight    = (14 * 1.2).sp,
        letterSpacing = 0.1.sp
    )

    /** 13sp · w500 · letterSpacing 0.2 · lineHeight 1.2 */
    val labelMedium = TextStyle(
        fontFamily    = sansFamily,
        fontSize      = 13.sp,
        fontWeight    = FontWeight.Medium,
        lineHeight    = (13 * 1.2).sp,
        letterSpacing = 0.2.sp
    )

    /** 11sp · w500 · lineHeight 1.2 */
    val labelSmall = TextStyle(
        fontFamily = sansFamily,
        fontSize   = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = (11 * 1.2).sp
    )

    // ── Specialised ───────────────────────────────────────────────────────────

    /** Chat message bubble text */
    val message = bodyMedium

    /** Inline code / code blocks */
    val code = TextStyle(
        fontFamily = codeFamily,
        fontSize   = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 19.sp
    )

    // ── Legacy aliases (so existing call-sites compile unchanged) ─────────────

    val h1    = displayLarge
    val h2    = displaySmall
    val h3    = headlineMedium
    val large = titleLarge

    val h1Bold = h1.copy(fontWeight = FontWeight.SemiBold)
    val h2Bold = h2.copy(fontWeight = FontWeight.SemiBold)
    val h3Bold = h3.copy(fontWeight = FontWeight.SemiBold)

    val body  = bodyMedium
    val small = bodySmall
    val mini  = labelMedium
    val tiny  = labelSmall

    // ── Material3 Typography binding ──────────────────────────────────────────

    val material = Typography(
        displayLarge   = displayLarge,
        displayMedium  = displayMedium,
        displaySmall   = displaySmall,
        headlineMedium = headlineMedium,
        titleLarge     = titleLarge,
        titleMedium    = titleMedium,
        bodyLarge      = bodyLarge,
        bodyMedium     = bodyMedium,
        bodySmall      = bodySmall,
        labelLarge     = labelLarge,
        labelMedium    = labelMedium,
        labelSmall     = labelSmall
    )
}