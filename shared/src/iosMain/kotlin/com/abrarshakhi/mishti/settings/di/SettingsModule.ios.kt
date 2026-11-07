package com.abrarshakhi.mishti.settings.di

import com.abrarshakhi.mishti.settings.domain.model.SettingsPlatformInfo
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle

internal actual val settingsPlatformModule: Module = module {
    single {
        val bundle = NSBundle.mainBundle
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
        val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: ""
        SettingsPlatformInfo(versionName = "$version ($build)", isDynamicColorAvailable = false)
    }
}
