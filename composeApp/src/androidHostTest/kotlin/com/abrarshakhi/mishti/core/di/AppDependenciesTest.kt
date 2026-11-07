package com.abrarshakhi.mishti.core.di

import com.abrarshakhi.mishti.core.domain.chat.repository.ChatRepository
import com.abrarshakhi.mishti.core.domain.chat.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.core.storage.chat.local.ChatDao
import com.abrarshakhi.mishti.core.storage.chat.repository.RoomChatRepository
import com.abrarshakhi.mishti.core.ui.drawer.DrawerController
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class AppDependenciesTest {
    @Test
    fun `composition resolves conversation dependencies using repository defaults`() {
        val dao = Proxy.newProxyInstance(ChatDao::class.java.classLoader, arrayOf(ChatDao::class.java)) {
                _,
                method,
                _,
            ->
            error("Dependency resolution must not query the database: ${method.name}")
        } as ChatDao
        val application = koinApplication {
            modules(composeAppModule, module { single<ChatDao> { dao } })
        }
        try {
            val koin = application.koin
            assertIs<RoomChatRepository>(koin.get<ChatRepository>())
            assertSame(koin.get<ChatRepository>(), koin.get<ChatRepository>())
            assertSame(koin.get<CreateNewSessionUseCase>(), koin.get<CreateNewSessionUseCase>())
            assertSame(koin.get<DrawerController>(), koin.get<DrawerController>())
        } finally {
            application.close()
        }
    }
}
