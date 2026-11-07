package com.abrarshakhi.mishti.core.designsystem.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme
import com.abrarshakhi.mishti.core.domain.theme.ThemeMode
import com.abrarshakhi.mishti.core.domain.theme.ThemeSettings
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme

@Composable
fun MishtiTheme(themeSettings: ThemeSettings = ThemeSettings(), content: @Composable () -> Unit) {
    val isDark = themeSettings.mode.isDark()

    SystemAppearance(isDark)

    val target = rememberThemeColorScheme(themeSettings.colorScheme, themeSettings.mode.isDark())
    val colorScheme = animateColorScheme(target)
    val family = themeSettings.font.fontFamily()
    val typography = remember(family) { typographyFor(family) }
    val icons = defaultMishtiIcons()
    CompositionLocalProvider(LocalMishtiIcons provides icons) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = typography,
        ) { content() }
    }
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

@Composable
expect fun rememberThemeColorScheme(scheme: AppColorScheme, dark: Boolean): ColorScheme

@Composable
internal fun rememberSeededColorScheme(seed: Color, dark: Boolean): ColorScheme = remember(seed, dark) {
    dynamicColorScheme(
        seedColor = seed,
        isDark = dark,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )
}
