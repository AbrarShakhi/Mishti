package com.abrarshakhi.mishti.features.settings.di

import com.abrarshakhi.mishti.common.ui.theme.isDynamicColorAvailable
import com.abrarshakhi.mishti.features.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            preferences = get(),
            isDynamicColorAvailable = isDynamicColorAvailable,
        )
    }
}
