package com.abrarshakhi.mishti.core.designsystem.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.abrarshakhi.mishti.core.designsystem.theme.rememberSeededColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme

@Composable
actual fun rememberThemeColorScheme(scheme: AppColorScheme, dark: Boolean): ColorScheme {
    val context = LocalContext.current
    val useDynamic = scheme == AppColorScheme.Dynamic &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return if (useDynamic) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberSeededColorScheme(scheme.seed, dark)
    }
}
