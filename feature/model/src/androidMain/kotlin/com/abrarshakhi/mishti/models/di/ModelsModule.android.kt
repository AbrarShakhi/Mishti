package com.abrarshakhi.mishti.models.di

import android.os.StatFs
import com.abrarshakhi.mishti.core.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.models.data.AndroidModelImportSource
import com.abrarshakhi.mishti.models.data.DownloadNotifier
import com.abrarshakhi.mishti.models.data.ModelImportSource
import com.abrarshakhi.mishti.models.data.ServiceDownloadNotifier
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfile
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfileProvider
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val modelsPlatformModule: Module = module {
    single {
        val directory = androidContext().filesDir.absolutePath
        ModelDirectories(directory.toPath(), FileSystem.SYSTEM) { StatFs(directory).availableBytes }
    }
    single<DeviceProfileProvider> {
        val capability = get<DeviceCapabilityProvider>()
        DeviceProfileProvider {
            DeviceProfile(
                capability.capability().totalMemoryBytes,
                if (android.os.Build.VERSION.SDK_INT >=
                    31
                ) {
                    android.os.Build.SOC_MODEL
                } else {
                    null
                },
                android.os.Build.HARDWARE,
                android.os.Build.BOARD,
            )
        }
    }
    single<ModelImportSource> { AndroidModelImportSource(androidContext()) }
    single<DownloadNotifier> { ServiceDownloadNotifier(androidContext()) }
}
