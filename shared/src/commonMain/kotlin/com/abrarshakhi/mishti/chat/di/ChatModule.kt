package com.abrarshakhi.mishti.chat.di

import com.abrarshakhi.mishti.chat.data.local.ChatDao
import com.abrarshakhi.mishti.chat.data.local.InMemoryChatDao
import com.abrarshakhi.mishti.chat.data.repository.RoomChatRepository
import com.abrarshakhi.mishti.chat.domain.repository.ChatRepository
import com.abrarshakhi.mishti.chat.domain.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.chat.ui.ChatRoute
import com.abrarshakhi.mishti.chat.ui.ChatViewModel
import com.abrarshakhi.mishti.chat.ui.SessionsViewModel
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatModule = module {
    single<ChatDao> { getOrNull() ?: InMemoryChatDao() }
    single<ChatRepository> { RoomChatRepository(dao = get()) }
    singleOf(::CreateNewSessionUseCase)
    viewModelOf(::SessionsViewModel)
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
