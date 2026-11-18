package com.abrarshakhi.mishti.settings.ui.viewmodels.contracts

import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.domain.llm.InferenceSettings
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppFont
import com.abrarshakhi.mishti.core.domain.theme.ThemeMode
import com.abrarshakhi.mishti.core.domain.theme.ThemeSettings
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiEffect
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiIntent
import com.abrarshakhi.mishti.core.ui.viewmodels.contracts.UiState
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultPerformance
import com.abrarshakhi.mishti.settings.domain.usecase.withDefaultSampling

sealed interface SettingsEffect : UiEffect
