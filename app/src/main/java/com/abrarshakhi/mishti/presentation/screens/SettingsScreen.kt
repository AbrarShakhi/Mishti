package com.abrarshakhi.mishti.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Settings screen.
 *
 * Reached from the Settings button pinned at the bottom of ChatHistoryScreen.
 * Full-screen destination pushed onto the back stack (not a dialog/overlay).
 *
 * Possible settings sections (TBD):
 *   - Theme  (system / light / dark)
 *   - Default model
 *   - Storage usage + delete all models
 *   - About / version
 *
 * @param onBack  Back arrow / system back → pop this entry off the stack.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    // TODO: implement UI
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen")
    }
}
