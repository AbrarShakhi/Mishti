package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.storage.preferences.data.DataStoreAppPreferences
import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import com.abrarshakhi.mishti.core.ui.AppViewModel
import com.abrarshakhi.mishti.core.ui.drawer.DrawerController
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::Navigator)
    singleOf(::DrawerController)
    singleOf(::SnackBarDispatcher)

    singleOf(::DataStoreAppPreferences).bind<AppPreferences>()
    viewModelOf(::AppViewModel)
}

internal expect val platformModule: Module
