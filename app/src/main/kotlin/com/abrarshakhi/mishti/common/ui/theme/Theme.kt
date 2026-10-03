package com.abrarshakhi.mishti.common.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.animateColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MishtiTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val target = rememberThemeColorScheme(settings.colorScheme, settings.mode.isDark())
    // Animated, so switching palette or light/dark cross-fades instead of snapping.
    val colorScheme = animateColorScheme(target)
    val typography = remember(settings.font) { typographyFor(settings.font.fontFamily()) }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = typography,
        content = content,
    )
}

/** Whether this mode resolves to a dark scheme right now. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * The first scheme is built in place, so the very first frame is already themed. Later changes
 * are built off the main thread and the theme cross-fades to them once they are ready, so
 * switching palette or mode never stalls the UI.
 */
@Composable
private fun rememberThemeColorScheme(scheme: AppColorScheme, dark: Boolean): ColorScheme {
    val context = LocalContext.current
    val initial = remember { appColorScheme(context, scheme, dark) }
    val target by produceState(initial, context, scheme, dark) {
        value = withContext(Dispatchers.Default) { appColorScheme(context, scheme, dark) }
    }
    return target
}

/** Seeded schemes are pure functions of seed and mode, and costly to generate. */
private val seededSchemes = ConcurrentHashMap<Pair<Color, Boolean>, ColorScheme>()

/**
 * The [ColorScheme] for [scheme]: the wallpaper colours for [AppColorScheme.Dynamic] where the
 * platform has them, otherwise a Material Kolor scheme generated from the palette's seed.
 *
 * Generating a seeded scheme solves dozens of tones, so each one is built once and kept. It is
 * safe to call from any thread, and callers that need a scheme they may not have seen yet should
 * call it off the main thread.
 */
fun appColorScheme(context: Context, scheme: AppColorScheme, dark: Boolean): ColorScheme =
    if (scheme == AppColorScheme.Dynamic && isDynamicColorAvailable) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        seededSchemes.getOrPut(scheme.seed to dark) {
            dynamicColorScheme(
                seedColor = scheme.seed,
                isDark = dark,
                style = PaletteStyle.TonalSpot,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
            )
        }
    }

val isDynamicColorAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
