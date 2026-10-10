package com.abrarshakhi.mishti.features.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class SplashViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {

    val startRoute: StateFlow<AppRouteKey?> =
        flow {
            val completed = preferences.hasCompletedOnboarding.first()
            emit(if (completed) AppRouteKey.Chat() else AppRouteKey.Onboarding)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}