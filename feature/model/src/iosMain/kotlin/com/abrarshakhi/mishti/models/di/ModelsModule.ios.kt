package com.abrarshakhi.mishti.models.di

import com.abrarshakhi.mishti.core.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.models.data.DownloadNotifier
import com.abrarshakhi.mishti.models.data.IosModelImportSource
import com.abrarshakhi.mishti.models.data.ModelImportSource
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfile
import com.abrarshakhi.mishti.models.data.catalog.DeviceProfileProvider
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual val modelsPlatformModule: Module = module {
    single {
        val manager = NSFileManager.defaultManager
        val directory =
            requireNotNull(manager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)?.path)
        ModelDirectories(directory.toPath(), FileSystem.SYSTEM) {
            (manager.attributesOfFileSystemForPath(directory, null)?.get(NSFileSystemFreeSize) as? NSNumber)
                ?.longLongValue ?: 0L
        }
    }
    single<DeviceProfileProvider> {
        val capability = get<DeviceCapabilityProvider>()
        DeviceProfileProvider {
            DeviceProfile(capability.capability().totalMemoryBytes, null, null, null)
        }
    }
    single<ModelImportSource> { IosModelImportSource() }
    single<DownloadNotifier> { DownloadNotifier.Noop }
}
