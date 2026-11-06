package com.abrarshakhi.mishti.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class SplashViewModel(private val preferences: AppPreferences) : ViewModel() {

    val startRoute: StateFlow<Screen?> =
        flow {
            val completed = preferences.hasCompletedOnboarding.first()
            emit(if (completed) Screen.Chat() else Screen.Onboarding)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
