package com.abrarshakhi.mishti.common.di

import androidx.room.Room
import com.abrarshakhi.mishti.common.data.database.ALL_MIGRATIONS
import com.abrarshakhi.mishti.common.data.database.DATABASE_NAME
import com.abrarshakhi.mishti.common.data.database.MishtiDatabase
import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.common.data.preferences.DataStoreAppPreferences
import com.abrarshakhi.mishti.common.llm.LlamaEngine
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.main.MainAppViewModel
import com.abrarshakhi.mishti.common.navigation.Navigator
import com.abrarshakhi.mishti.common.ui.snackbar.SnackBarDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single { Navigator() }
        single {
            Room
                .databaseBuilder(androidContext(), MishtiDatabase::class.java, DATABASE_NAME)
                .addMigrations(*ALL_MIGRATIONS)
                .build()
        }

        single { get<MishtiDatabase>().chatDao() }

        single { get<MishtiDatabase>().installedModelDao() }

        single<AppPreferences> { DataStoreAppPreferences(androidContext()) }

        single { SnackBarDispatcher() }

        single<LlmEngine> { LlamaEngine() }

        viewModel { MainAppViewModel(preferences = get(), engine = get(), selectedModel = get()) }
    }
