package com.abrarshakhi.mishti.settings.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_sampling_defaults
import com.abrarshakhi.mishti.feature.settings.resources.settings_temperature
import com.abrarshakhi.mishti.feature.settings.resources.settings_temperature_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_k
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_k_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_p
import com.abrarshakhi.mishti.feature.settings.resources.settings_top_p_hint
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

internal fun decimal(value: Float): String {
    val hundredths = (value * 100).roundToInt()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}"
}
