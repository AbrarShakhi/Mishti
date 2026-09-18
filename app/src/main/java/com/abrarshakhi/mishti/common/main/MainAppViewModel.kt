package com.abrarshakhi.mishti.common.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainAppViewModel(
    private val preferences: AppPreferences,
    private val selectedModel: SelectedModelSource,
) : ViewModel() {

    val themeSettings: StateFlow<ThemeSettings> = preferences.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeSettings(),
    )

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
            ) { handle -> handle }.collectLatest { (handle, options) ->
            }
        }
    }
}
