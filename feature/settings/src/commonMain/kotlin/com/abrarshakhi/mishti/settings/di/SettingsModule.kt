package com.abrarshakhi.mishti.settings.di

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.settings.data.ComposeDocumentRepository
import com.abrarshakhi.mishti.settings.domain.model.SettingsPlatformInfo
import com.abrarshakhi.mishti.settings.domain.repository.DocumentRepository
import com.abrarshakhi.mishti.settings.domain.usecase.ReadDocumentUseCase
import com.abrarshakhi.mishti.settings.domain.usecase.UpdateInferenceSettingsUseCase
import com.abrarshakhi.mishti.settings.ui.AppearanceSettingsRoute
import com.abrarshakhi.mishti.settings.ui.ChatSettingsRoute
import com.abrarshakhi.mishti.settings.ui.DocumentRoute
import com.abrarshakhi.mishti.settings.ui.DocumentViewModel
import com.abrarshakhi.mishti.settings.ui.GenerationSettingsRoute
import com.abrarshakhi.mishti.settings.ui.PerformanceSettingsRoute
import com.abrarshakhi.mishti.settings.ui.SettingsRoute
import com.abrarshakhi.mishti.settings.ui.SettingsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsModule = module {
    includes(settingsPlatformModule)
    single<DocumentRepository> { ComposeDocumentRepository() }
    singleOf(::ReadDocumentUseCase)
    singleOf(::UpdateInferenceSettingsUseCase)
    viewModel {
        SettingsViewModel(
            preferences = get(),
            snackBar = get(),
            engine = get(),
            isDynamicColorAvailable = get<SettingsPlatformInfo>().isDynamicColorAvailable,
            updateInferenceSettings = get(),
        )
    }
    viewModel { parameters -> DocumentViewModel(parameters.get<AppDocument>(), get()) }
    navigation<Screen.Settings> { SettingsRoute() }
    navigation<Screen.AppearanceSettings> { AppearanceSettingsRoute() }
    navigation<Screen.ChatSettings> { ChatSettingsRoute() }
    navigation<Screen.GenerationSettings> { GenerationSettingsRoute() }
    navigation<Screen.PerformanceSettings> { PerformanceSettingsRoute() }
    navigation<Screen.Document> { screen -> DocumentRoute(screen.document) }
}

internal expect val settingsPlatformModule: Module
