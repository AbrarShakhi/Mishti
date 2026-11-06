package com.abrarshakhi.mishti

import android.app.Application
import com.abrarshakhi.mishti.core.di.setupKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MishtiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MishtiApplication)
        }
    }
}
