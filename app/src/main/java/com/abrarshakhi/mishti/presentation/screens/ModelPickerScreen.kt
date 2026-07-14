package com.abrarshakhi.mishti.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Model picker screen / bottom-sheet.
 *
 * Opened when the user taps the model name in the top-bar centre of ChatScreen.
 * Lists downloaded models and links to the Models download screen.
 *
 * Structure (shown as a bottom-sheet or full-screen on small phones):
 *   ┌─────────────────────────────────┐
 *   │  Select model            [✕]   │  ← header + dismiss
 *   │                                 │
 *   │  ● TinyLlama 1.1B  (active)    │
 *   │    Phi-3 Mini 3.8B             │  ← downloaded model list
 *   │    Gemma 3 1B                  │
 *   │                                 │
 *   │  [+ Download more models]       │  ← links to ModelsScreen (future)
 *   └─────────────────────────────────┘
 *
 * @param onDismiss        Close without changing model.
 * @param onModelSelected  User picked a model → close + switch active model.
 */
@Composable
fun ModelPickerScreen(
    onDismiss: () -> Unit,
    onModelSelected: (modelId: String) -> Unit,
) {
    // TODO: implement UI
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Model Picker Screen")
    }
}
