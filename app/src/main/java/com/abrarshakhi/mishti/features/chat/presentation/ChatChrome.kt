package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.main.MainAppViewModel
import com.abrarshakhi.mishti.common.main.ScreenChrome
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun chatChrome(): ScreenChrome = ScreenChrome(
    title = "Chat",
    topBar = { scope ->
        val mainAppViewModel: MainAppViewModel = koinViewModel()
        val engineState by mainAppViewModel.engineState.collectAsStateWithLifecycle()

        TopAppBar(
            title = { ChatTitle(engineState) },
            scrollBehavior = scope.scrollBehavior,
            navigationIcon = {
                IconButton(onClick = scope.openDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open conversations",
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {  },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New chat",
                    )
                }
            },
        )
    },
)

@Composable
private fun ChatTitle(engineState: EngineState) {
    Row {
        Text("Chat", modifier = Modifier.alignByBaseline())
        Spacer(Modifier.width(8.dp))
        Text(
            text = engineState.modelLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alignByBaseline()
                .weight(1f, fill = false),
        )
    }
}

private fun EngineState.modelLabel(): String = when (this) {
    is EngineState.Ready -> model.name
    is EngineState.Loading -> "Loading…"
    is EngineState.Failed -> "Model failed to load"
    EngineState.Idle -> "No model selected"
}
