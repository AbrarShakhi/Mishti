package com.abrarshakhi.mishti.features.splash.di

import com.abrarshakhi.mishti.features.splash.presentation.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashModule = module {
    viewModel { SplashViewModel(preferences = get()) }
}