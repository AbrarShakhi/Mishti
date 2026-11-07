package com.abrarshakhi.mishti.models.di

import com.abrarshakhi.mishti.core.domain.llm.SelectedModelSource
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.models.data.DefaultModelRepository
import com.abrarshakhi.mishti.models.data.ModelDownloader
import com.abrarshakhi.mishti.models.data.ModelImporter
import com.abrarshakhi.mishti.models.data.ModelStorage
import com.abrarshakhi.mishti.models.data.PreferencesSelectedModelSource
import com.abrarshakhi.mishti.models.data.catalog.CatalogFetcher
import com.abrarshakhi.mishti.models.data.catalog.CatalogStore
import com.abrarshakhi.mishti.models.data.catalog.DefaultCatalogRepository
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfileProvider
import com.abrarshakhi.mishti.models.data.catalog.FileCatalogStore
import com.abrarshakhi.mishti.models.data.catalog.KtorCatalogFetcher
import com.abrarshakhi.mishti.models.data.catalog.MishtiCatalogParser
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.repository.CatalogRepository
import com.abrarshakhi.mishti.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.models.ui.ModelsRoute
import com.abrarshakhi.mishti.models.ui.ModelsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import okio.FileSystem
import okio.Path
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.dsl.onClose

class ModelDirectories(val root: Path, val fileSystem: FileSystem, val availableBytes: () -> Long)
private class ModelRepositoryScope : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO)

@OptIn(KoinExperimentalAPI::class)
val modelsModule = module {
    includes(modelsPlatformModule)
    single { ModelRepositoryScope() } onClose { it?.cancel() }
    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = null
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
        }
    } onClose { it?.close() }
    single {
        val directories = get<ModelDirectories>()
        ModelStorage(directories.fileSystem, directories.root / "models", directories.availableBytes)
    }
    single<CatalogStore> {
        FileCatalogStore(get<ModelDirectories>().fileSystem, get<ModelDirectories>().root / "catalogs")
    }
    single<CatalogFetcher> { KtorCatalogFetcher(get()) }
    single<CatalogRepository> {
        val device = get<DeviceProfileProvider>()
        DefaultCatalogRepository(get(), get(), device = device::profile)
    }
    single { ModelDownloader(get(), get()) }
    single { ModelImporter(get(), get()) }
    single<ModelRepository> {
        val store = get<CatalogStore>()
        DefaultModelRepository(
            dao = get(),
            storageManager = get(),
            downloader = get(),
            importer = get(),
            preferences = get(),
            scope = get<ModelRepositoryScope>(),
            knownModels = {
                store.read(CatalogSource.MishtirBhandar)?.text?.let {
                    runCatching { MishtiCatalogParser.parse(it).models }.getOrNull()
                }.orEmpty()
            },
            notifier = get(),
        )
    }
    single<SelectedModelSource> { PreferencesSelectedModelSource(get(), get(), get()) }
    viewModelOf(::ModelsViewModel)
    navigation<Screen.Models> { ModelsRoute() }
}

internal expect val modelsPlatformModule: Module
