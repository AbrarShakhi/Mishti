package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.domain.chat.repository.ChatRepository
import com.abrarshakhi.mishti.core.domain.chat.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.core.domain.preferences.AppPreferences
import com.abrarshakhi.mishti.core.session.ui.SessionsViewModel
import com.abrarshakhi.mishti.core.storage.chat.repository.RoomChatRepository
import com.abrarshakhi.mishti.core.storage.preferences.data.DataStoreAppPreferences
import com.abrarshakhi.mishti.core.ui.AppViewModel
import com.abrarshakhi.mishti.core.ui.drawer.DrawerController
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val composeAppModule = module {
    single<ChatRepository> { RoomChatRepository(get()) }
    singleOf(::CreateNewSessionUseCase)
    viewModel { SessionsViewModel(get(), get(), get()) }
    singleOf(::Navigator)
    singleOf(::DrawerController)
    singleOf(::SnackBarDispatcher)

    singleOf(::DataStoreAppPreferences).bind<AppPreferences>()
    viewModelOf(::AppViewModel)
}

internal expect val platformModule: Module
