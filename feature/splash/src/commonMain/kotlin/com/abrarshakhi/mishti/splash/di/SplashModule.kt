package com.abrarshakhi.mishti.splash.di

import com.abrarshakhi.mishti.core.ui.utils.Screen
import com.abrarshakhi.mishti.splash.ui.SplashRoute
import com.abrarshakhi.mishti.splash.ui.viewmodels.SplashViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val splashModule = module {
    navigation<Screen.Splash> { SplashRoute() }
    viewModelOf(::SplashViewModel)
}
