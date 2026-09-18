package com.abrarshakhi.mishti.common.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainAppViewModel(
    private val preferences: AppPreferences,
    private val engine: LlmEngine,
    private val selectedModel: SelectedModelSource,
) : ViewModel() {

    val themeSettings: StateFlow<ThemeSettings> = preferences.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeSettings(),
    )

    val engineState: StateFlow<EngineState> = engine.state

    val startRoute: StateFlow<AppRouteKey?> = flow {
        val completed = preferences.hasCompletedOnboarding.first()
        emit(if (completed) AppRouteKey.Chat() else AppRouteKey.Onboarding)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            combine(
                selectedModel.selected(),
                preferences.inferenceSettings.map { it.engine }.distinctUntilChanged(),
            ) { handle, options -> handle to options }.collectLatest { (handle, options) ->
                if (handle == null) engine.unload() else engine.load(handle, options)
            }
        }
    }
}
