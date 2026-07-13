package com.abrarshakhi.mishti.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abrarshakhi.mishti.MishtiApp
import com.abrarshakhi.mishti.presentation.screens.ChatScreen
import com.abrarshakhi.mishti.presentation.screens.ModelsScreen
import com.abrarshakhi.mishti.presentation.theme.MishtiTheme
import com.abrarshakhi.mishti.presentation.viewmodels.ChatViewModel
import com.abrarshakhi.mishti.presentation.viewmodels.ModelsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as MishtiApp
            setContent {
                MishtiTheme {
                    MishtiNavHost(app = app)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Simple in-memory navigation.
//
// We keep navigation minimal — just two screens.
// (Later need to swap this for Navigation Compose if the app grows.)
// ─────────────────────────────────────────────────────────────────────────────
private sealed class Screen {
    data object Models : Screen()
    data class  Chat(val modelId: String, val modelName: String) : Screen()
}

@Composable
private fun MishtiNavHost(app: MishtiApp) {
    var screen by remember { mutableStateOf<Screen>(Screen.Models) }

    when (val s = screen) {

        is Screen.Models -> {
            val vm: ModelsViewModel = viewModel(
                factory = ModelsViewModel.Factory(app.modelRepository)
            )
            val downloadedModels by vm.downloadedModels.collectAsStateWithLifecycle()
            val uiState          by vm.uiState.collectAsStateWithLifecycle()

            ModelsScreen(
                availableModels  = vm.availableModels,
                downloadedModels = downloadedModels,
                uiState          = uiState,
                onDownload       = { vm.downloadModel(it) },
                onDelete         = { vm.deleteModel(it) },
                onChat           = { model ->
                    screen = Screen.Chat(
                        modelId   = model.id,
                        modelName = model.name
                    )
                }
            )
        }

        is Screen.Chat -> {
            val vm: ChatViewModel = viewModel(
                key     = s.modelId,   // separate VM instance per model
                factory = ChatViewModel.Factory(app.llamaEngine, app.modelRepository)
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(s.modelId) { vm.loadModel(s.modelId) }

            ChatScreen(
                modelName = s.modelName,
                uiState   = uiState,
                onSend    = { vm.sendMessage(it) },
                onStop    = { vm.stopGeneration() },
                onClear   = { vm.clearChat() }
            )
        }
    }
}