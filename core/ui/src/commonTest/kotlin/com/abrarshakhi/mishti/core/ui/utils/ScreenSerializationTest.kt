package com.abrarshakhi.mishti.core.ui.utils

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenSerializationTest {
    @Test
    fun `navigation keys retain their arguments after serialization`() {
        val screens = listOf(
            Screen.Splash,
            Screen.Onboarding,
            Screen.Chat(),
            Screen.Chat("saved-session"),
            Screen.Models,
            Screen.Settings,
            Screen.AppearanceSettings,
            Screen.ChatSettings,
            Screen.GenerationSettings,
            Screen.PerformanceSettings,
        ) + AppDocument.entries.map(Screen::Document)
        for (screen in screens) {
            val saved = Json.encodeToString(Screen.serializer(), screen)
            assertEquals(screen, Json.decodeFromString(Screen.serializer(), saved))
        }
    }

    @Test
    fun `navigation keys retain their wire format from the previous package`() {
        val previousPrefix = "com.abrarshakhi.mishti.core.ui.navigation.Screen"
        val savedScreens = listOf(
            """{"type":"$previousPrefix.Splash"}""" to Screen.Splash,
            """{"type":"$previousPrefix.Onboarding"}""" to Screen.Onboarding,
            """{"type":"$previousPrefix.Chat"}""" to Screen.Chat(),
            """{"type":"$previousPrefix.Chat","sessionId":"saved-session"}""" to Screen.Chat("saved-session"),
            """{"type":"$previousPrefix.Models"}""" to Screen.Models,
            """{"type":"$previousPrefix.Settings"}""" to Screen.Settings,
            """{"type":"$previousPrefix.AppearanceSettings"}""" to Screen.AppearanceSettings,
            """{"type":"$previousPrefix.ChatSettings"}""" to Screen.ChatSettings,
            """{"type":"$previousPrefix.GenerationSettings"}""" to Screen.GenerationSettings,
            """{"type":"$previousPrefix.PerformanceSettings"}""" to Screen.PerformanceSettings,
            """{"type":"$previousPrefix.Document","document":"Privacy"}""" to Screen.Document(AppDocument.Privacy),
        )
        for ((saved, screen) in savedScreens) {
            assertEquals(screen, Json.decodeFromString(Screen.serializer(), saved))
            assertEquals(saved, Json.encodeToString(Screen.serializer(), screen))
        }
    }
}
