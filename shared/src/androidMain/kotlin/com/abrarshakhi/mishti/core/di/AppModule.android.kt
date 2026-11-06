package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.device.AndroidDeviceCapabilityProvider
import com.abrarshakhi.mishti.core.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.core.llm.data.AndroidLlamaBridge
import com.abrarshakhi.mishti.core.llm.data.LlamaEngine
import com.abrarshakhi.mishti.core.llm.domain.LlamaBridge
import com.abrarshakhi.mishti.core.llm.domain.LlmEngine
import com.abrarshakhi.mishti.core.storage.preferences.data.produceDataStorePath
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        singleOf(::produceDataStorePath)
        singleOf(::AndroidDeviceCapabilityProvider).bind<DeviceCapabilityProvider>()
        singleOf(::AndroidLlamaBridge).bind<LlamaBridge>()
        single<LlmEngine> { LlamaEngine(bridge = get()) }
    }
