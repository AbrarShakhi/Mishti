package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.domain.chat.repository.ChatRepository
import com.abrarshakhi.mishti.core.domain.chat.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.core.domain.preferences.AppPreferences
import com.abrarshakhi.mishti.core.storage.chat.repository.RoomChatRepository
import com.abrarshakhi.mishti.core.storage.preferences.data.DataStoreAppPreferences
import com.abrarshakhi.mishti.core.ui.utils.DrawerController
import com.abrarshakhi.mishti.core.ui.utils.Navigator
import com.abrarshakhi.mishti.core.ui.utils.SnackBarDispatcher
import com.abrarshakhi.mishti.core.ui.viewmodels.AppViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val composeAppModule = module {
    single<ChatRepository> { RoomChatRepository(get()) }
    singleOf(::CreateNewSessionUseCase)
    singleOf(::Navigator)
    singleOf(::DrawerController)
    singleOf(::SnackBarDispatcher)

    singleOf(::DataStoreAppPreferences).bind<AppPreferences>()
    viewModelOf(::AppViewModel)
}

internal expect val platformModule: Module
