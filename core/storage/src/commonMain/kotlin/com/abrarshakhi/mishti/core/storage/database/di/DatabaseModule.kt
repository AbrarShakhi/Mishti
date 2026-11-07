package com.abrarshakhi.mishti.core.storage.database.di

import com.abrarshakhi.mishti.core.storage.chat.local.ChatDao
import com.abrarshakhi.mishti.core.storage.database.MishtiDatabase
import com.abrarshakhi.mishti.core.storage.database.createMishtiDatabase
import com.abrarshakhi.mishti.core.storage.models.InstalledModelDao
import org.koin.dsl.module
import org.koin.dsl.onClose

val databaseModule = module {
    single<MishtiDatabase> { createMishtiDatabase(builder = get()) } onClose { it?.close() }
    single<InstalledModelDao> { get<MishtiDatabase>().installedModelDao() }
    single<ChatDao> { get<MishtiDatabase>().chatDao() }
}
