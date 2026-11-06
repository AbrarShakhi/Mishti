package com.abrarshakhi.mishti.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.designsystem.theme.ThemeSettings
import com.abrarshakhi.mishti.core.llm.domain.LlmEngine
import com.abrarshakhi.mishti.core.llm.domain.SelectedModelSource
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val preferences: AppPreferences,
    private val engine: LlmEngine,
    private val selectedModel: SelectedModelSource,
) : ViewModel() {
    val themeSettings: StateFlow<ThemeSettings> = preferences.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeSettings(),
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
