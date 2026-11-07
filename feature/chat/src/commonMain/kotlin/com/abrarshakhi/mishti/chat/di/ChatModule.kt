package com.abrarshakhi.mishti.chat.di

import com.abrarshakhi.mishti.chat.ui.ChatRoute
import com.abrarshakhi.mishti.chat.ui.ChatViewModel
import com.abrarshakhi.mishti.core.ui.navigation.Screen
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
        )
    }
    navigation<Screen.Chat> { screen ->
        ChatRoute(sessionId = screen.sessionId)
    }
}
