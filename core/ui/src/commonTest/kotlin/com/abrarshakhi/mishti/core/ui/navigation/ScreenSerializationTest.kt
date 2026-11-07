package com.abrarshakhi.mishti.core.ui.navigation

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
}
