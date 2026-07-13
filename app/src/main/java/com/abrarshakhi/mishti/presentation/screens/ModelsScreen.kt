package com.abrarshakhi.mishti.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.data.db.ModelEntity
import com.abrarshakhi.mishti.data.repository.AvailableModel
import com.abrarshakhi.mishti.presentation.viewmodels.ModelsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    availableModels:  List<AvailableModel>,
    downloadedModels: List<ModelEntity>,
    uiState:          ModelsUiState,
    onDownload:       (AvailableModel) -> Unit,
    onDelete:         (ModelEntity) -> Unit,
    onChat:           (ModelEntity) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Models") }) }
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Downloaded section ───────────────────────────────────────────
            if (downloadedModels.isNotEmpty()) {
                item {
                    Text(
                        "On this device",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
                items(downloadedModels) { model ->
                    DownloadedModelCard(
                        model    = model,
                        onDelete = { onDelete(model) },
                        onChat   = { onChat(model) }
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
            }

            // ── Available to download ────────────────────────────────────────
            item {
                Text(
                    "Available to download",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }

            items(availableModels) { model ->
                val isDownloaded = downloadedModels.any { it.id == model.id }
                val progress     = uiState.downloadProgress[model.id]
                val error        = uiState.downloadError[model.id]

                AvailableModelCard(
                    model        = model,
                    isDownloaded = isDownloaded,
                    progress     = progress,
                    error        = error,
                    onDownload   = { onDownload(model) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DownloadedModelCard(
    model:    ModelEntity,
    onDelete: () -> Unit,
    onChat:   () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(model.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    formatBytes(model.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.width(4.dp))
            FilledIconButton(onClick = onChat) {
                Icon(Icons.Default.Chat, contentDescription = "Chat")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AvailableModelCard(
    model:        AvailableModel,
    isDownloaded: Boolean,
    progress:     Int?,
    error:        String?,
    onDownload:   () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatBytes(model.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                when {
                    isDownloaded -> {
                        // Already downloaded — show a tick badge
                        AssistChip(
                            onClick = {},
                            label   = { Text("Downloaded") }
                        )
                    }
                    progress != null -> {
                        // Downloading — show progress
                        CircularProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    else -> {
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                        }
                    }
                }
            }

            // Download progress bar
            if (progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "$progress%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Error message
            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Error: $error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000L -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000L     -> "%.0f MB".format(bytes / 1_000_000.0)
    else                    -> "%.0f KB".format(bytes / 1_000.0)
}