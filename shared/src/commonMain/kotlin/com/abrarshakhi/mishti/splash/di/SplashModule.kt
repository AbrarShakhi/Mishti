package com.abrarshakhi.mishti.splash.di

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val splashModule = module {
    navigation<Screen.Splash> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Splash Screen Placeholder")
        }
    }
}
