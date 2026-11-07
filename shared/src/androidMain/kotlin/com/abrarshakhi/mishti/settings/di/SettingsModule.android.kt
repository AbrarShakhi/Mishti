package com.abrarshakhi.mishti.settings.di

import android.os.Build
import com.abrarshakhi.mishti.settings.domain.model.SettingsPlatformInfo
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val settingsPlatformModule: Module = module {
    single {
        val context = androidContext()
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        SettingsPlatformInfo(
            versionName = "${info.versionName.orEmpty()} (${info.longVersionCode})",
            isDynamicColorAvailable = Build.VERSION.SDK_INT >= 31,
        )
    }
}
