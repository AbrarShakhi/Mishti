package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.device.DeviceCapabilityProvider
import com.abrarshakhi.mishti.core.device.IosDeviceCapabilityProvider
import com.abrarshakhi.mishti.core.llm.data.LlamaBridgeRegistry
import com.abrarshakhi.mishti.core.llm.data.LlamaEngine
import com.abrarshakhi.mishti.core.llm.data.ScriptedLlmEngine
import com.abrarshakhi.mishti.core.llm.domain.LlmEngine
import com.abrarshakhi.mishti.core.storage.preferences.data.produceDataStorePath
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        singleOf(::produceDataStorePath)
        singleOf(::IosDeviceCapabilityProvider).bind<DeviceCapabilityProvider>()
        single<LlmEngine> {
            val bridge = LlamaBridgeRegistry.bridge
            if (bridge != null) {
                LlamaEngine(bridge = bridge)
            } else {
                ScriptedLlmEngine()
            }
        }
    }
