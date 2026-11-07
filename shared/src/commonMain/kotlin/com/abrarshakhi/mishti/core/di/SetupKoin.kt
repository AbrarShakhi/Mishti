package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.chat.di.chatModule
import com.abrarshakhi.mishti.core.storage.database.di.databaseModule
import com.abrarshakhi.mishti.models.di.modelsModule
import com.abrarshakhi.mishti.onboarding.di.onboardingModule
import com.abrarshakhi.mishti.settings.di.settingsModule
import com.abrarshakhi.mishti.splash.di.splashModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun setupKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            platformModule,
            databaseModule,
            splashModule,
            onboardingModule,
            chatModule,
            modelsModule,
            settingsModule,
        )
    }
}
