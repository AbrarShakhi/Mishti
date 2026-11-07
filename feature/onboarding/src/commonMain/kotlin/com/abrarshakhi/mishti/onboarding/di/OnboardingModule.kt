package com.abrarshakhi.mishti.onboarding.di

import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.onboarding.ui.OnboardingRoute
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val onboardingModule = module {
    navigation<Screen.Onboarding> { OnboardingRoute() }
}
