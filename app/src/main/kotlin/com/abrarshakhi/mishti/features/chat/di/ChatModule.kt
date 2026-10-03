package com.abrarshakhi.mishti.features.chat.di

import com.abrarshakhi.mishti.features.chat.data.repository.RoomChatRepository
import com.abrarshakhi.mishti.features.chat.domain.repository.ChatRepository
import com.abrarshakhi.mishti.features.chat.presentation.ChatViewModel
import com.abrarshakhi.mishti.features.chat.presentation.SessionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val chatModule = module {

    single<ChatRepository> { RoomChatRepository(dao = get()) }

    viewModel { SessionsViewModel(repository = get(), snackbar = get()) }

    viewModel { parameters ->
        ChatViewModel(
            requestedSessionId = parameters.getOrNull(),
            repository = get(),
            engine = get(),
            preferences = get(),
        )
    }
}
