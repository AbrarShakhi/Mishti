package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.core.device.IosDeviceCapabilityProvider
import com.abrarshakhi.mishti.core.storage.preferences.data.produceDataStorePath
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        singleOf(::produceDataStorePath)
        singleOf(::IosDeviceCapabilityProvider).bind<DeviceCapabilityProvider>()
    }
