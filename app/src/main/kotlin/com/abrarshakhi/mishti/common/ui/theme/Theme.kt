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
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
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
    val colorScheme = animateColorScheme(target)
    val typography = remember(settings.font) { typographyFor(settings.font.fontFamily()) }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = typography,
        content = content,
    )
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

@Composable
private fun rememberThemeColorScheme(scheme: AppColorScheme, dark: Boolean): ColorScheme {
    val context = LocalContext.current
    val initial = remember { appColorScheme(context, scheme, dark) }
    val target by produceState(initial, context, scheme, dark) {
        value = withContext(Dispatchers.Default) { appColorScheme(context, scheme, dark) }
    }
    return target
}

private val seededSchemes = ConcurrentHashMap<Pair<Color, Boolean>, ColorScheme>()

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
