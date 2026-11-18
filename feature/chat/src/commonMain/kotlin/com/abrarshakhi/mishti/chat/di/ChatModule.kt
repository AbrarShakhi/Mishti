package com.abrarshakhi.mishti.chat.di

import com.abrarshakhi.mishti.chat.ui.ChatRoute
import com.abrarshakhi.mishti.chat.ui.viewmodels.ChatViewModel
import com.abrarshakhi.mishti.core.ui.utils.Screen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatModule = module {
    viewModel { parameters ->
        ChatViewModel(
            requestedSessionId = parameters.getOrNull(),
            repository = get(),
            engine = get(),
            preferences = get(),
            speechRecognizer = get(),
            modelSelector = get(),
        )
    }
    navigation<Screen.Chat> { screen ->
        ChatRoute(sessionId = screen.sessionId)
    }
}
