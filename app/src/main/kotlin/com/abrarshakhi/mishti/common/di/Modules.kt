package com.abrarshakhi.mishti.common.di

import com.abrarshakhi.mishti.features.chat.di.chatModule
import com.abrarshakhi.mishti.features.models.di.modelsModule
import com.abrarshakhi.mishti.features.settings.di.settingsModule
import com.abrarshakhi.mishti.features.splash.di.splashModule
import org.koin.core.module.Module

val appModules: List<Module> =
    listOf(
        appModule,
        splashModule,
        chatModule,
        modelsModule,
        settingsModule,
    )
