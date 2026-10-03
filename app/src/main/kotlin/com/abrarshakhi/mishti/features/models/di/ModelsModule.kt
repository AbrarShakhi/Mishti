package com.abrarshakhi.mishti.features.models.di

import com.abrarshakhi.mishti.common.device.AndroidDeviceCapabilityProvider
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.features.models.data.DefaultModelRepository
import com.abrarshakhi.mishti.features.models.data.DownloadNotifier
import com.abrarshakhi.mishti.features.models.data.ModelDownloader
import com.abrarshakhi.mishti.features.models.data.ModelStorage
import com.abrarshakhi.mishti.features.models.data.PreferencesSelectedModelSource
import com.abrarshakhi.mishti.features.models.data.ServiceDownloadNotifier
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.presentation.ModelsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val modelsModule = module {

    single {
        HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = null
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
        }
    }

    single<DeviceCapabilityProvider> { AndroidDeviceCapabilityProvider(androidContext()) }

    single { ModelStorage(androidContext()) }
    single<DownloadNotifier> { ServiceDownloadNotifier(androidContext()) }
    single { ModelDownloader(client = get(), storage = get()) }

    single<SelectedModelSource> {
        PreferencesSelectedModelSource(preferences = get(), storage = get())
    }

    single<ModelRepository> {
        DefaultModelRepository(
            storageManager = get(),
            downloader = get(),
            preferences = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            notifier = get(),
        )
    }

    viewModel {
        ModelsViewModel(repository = get(), snackbar = get(), capabilityProvider = get())
    }
}
