package com.abrarshakhi.mishti.features.models.di

import android.os.Build
import com.abrarshakhi.mishti.common.device.AndroidDeviceCapabilityProvider
import com.abrarshakhi.mishti.common.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.common.llm.SelectedModelSource
import com.abrarshakhi.mishti.features.models.data.DefaultModelRepository
import com.abrarshakhi.mishti.features.models.data.DownloadNotifier
import com.abrarshakhi.mishti.features.models.data.ModelDownloader
import com.abrarshakhi.mishti.features.models.data.ModelImporter
import com.abrarshakhi.mishti.features.models.data.ModelStorage
import com.abrarshakhi.mishti.features.models.data.PreferencesSelectedModelSource
import com.abrarshakhi.mishti.features.models.data.ServiceDownloadNotifier
import com.abrarshakhi.mishti.features.models.data.catalog.CatalogFetcher
import com.abrarshakhi.mishti.features.models.data.catalog.CatalogStore
import com.abrarshakhi.mishti.features.models.data.catalog.DefaultCatalogRepository
import com.abrarshakhi.mishti.features.models.data.catalog.DeviceProfile
import com.abrarshakhi.mishti.features.models.data.catalog.FileCatalogStore
import com.abrarshakhi.mishti.features.models.data.catalog.KtorCatalogFetcher
import com.abrarshakhi.mishti.features.models.data.catalog.MishtiCatalogParser
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.repository.CatalogRepository
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
    single { ModelImporter(context = androidContext(), storage = get()) }

    single<CatalogStore> { FileCatalogStore(androidContext()) }
    single<CatalogFetcher> { KtorCatalogFetcher(client = get()) }

    single<CatalogRepository> {
        val capability = get<DeviceCapabilityProvider>()
        DefaultCatalogRepository(
            store = get(),
            fetcher = get(),
            device = {
                DeviceProfile(
                    totalRamBytes = capability.capability().totalMemoryBytes,
                    socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else null,
                    hardware = Build.HARDWARE,
                    board = Build.BOARD,
                )
            },
        )
    }

    single<SelectedModelSource> {
        PreferencesSelectedModelSource(preferences = get(), dao = get(), storage = get())
    }

    single<ModelRepository> {
        val store = get<CatalogStore>()
        DefaultModelRepository(
            dao = get(),
            storageManager = get(),
            downloader = get(),
            importer = get(),
            preferences = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            knownModels = {
                store.bundled(CatalogSource.MistirBhandar)
                    ?.let { runCatching { MishtiCatalogParser.parse(it).models }.getOrNull() }
                    .orEmpty()
            },
            notifier = get(),
        )
    }

    viewModel {
        ModelsViewModel(
            repository = get(),
            catalogs = get(),
            snackbar = get(),
            capabilityProvider = get(),
        )
    }
}
