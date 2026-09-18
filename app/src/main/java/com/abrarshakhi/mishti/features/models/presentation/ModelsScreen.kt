package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.features.models.domain.model.ModelCatalog
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.model.ModelStatus
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage

@Composable
fun ModelsScreen(
    state: ModelsUiState,
    onIntent: (ModelsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.capability is DeviceCapability.UnsupportedLowMemory) {
            item { UnsupportedDeviceNotice(state.capability) }
        }

        item { StorageSummary(state.storage) }

        items(items = state.entries, key = { it.model.id }) { entry ->
            ModelCard(
                entry = entry,
                isSelected = entry.model.id == state.selectedModelId,
                onIntent = onIntent,
            )
        }
    }

    state.deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { onIntent(ModelsIntent.DeleteCancelled) },
            title = { Text("Delete ${entry.model.name}?") },
            text = {
                Text(
                    "The file will be removed from this device. You can download it again later."
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(ModelsIntent.DeleteConfirmed) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ModelsIntent.DeleteCancelled) }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun UnsupportedDeviceNotice(capability: DeviceCapability.UnsupportedLowMemory) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This device is not supported", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Running a model needs about " +
                    "${formatBytes(capability.requiredMemoryBytes)} of memory; this device has " +
                    "${formatBytes(capability.totalMemoryBytes)}.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun StorageSummary(storage: StorageUsage) {
    Text(
        text = "${formatBytes(storage.usedBytes)} used · " +
            "${formatBytes(storage.availableBytes)} free",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun ModelCard(
    entry: ModelEntry,
    isSelected: Boolean,
    onIntent: (ModelsIntent) -> Unit,
) {
    Card(
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.model.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${entry.model.parameters} · ${entry.model.quantization} · " +
                            formatBytes(entry.model.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ModelAction(entry = entry, isSelected = isSelected, onIntent = onIntent)
            }

            Text(
                text = entry.model.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (isSelected) {
                Text(
                    text = "In use",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            when (val status = entry.status) {
                is ModelStatus.Downloading -> DownloadProgressRow(status)
                ModelStatus.Verifying -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                    Text("Verifying…", style = MaterialTheme.typography.bodySmall)
                }
                is ModelStatus.Failed -> Text(
                    text = status.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun DownloadProgressRow(status: ModelStatus.Downloading) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        LinearProgressIndicator(
            progress = { status.fraction },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${formatBytes(status.downloadedBytes)} of ${formatBytes(status.totalBytes)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ModelAction(
    entry: ModelEntry,
    isSelected: Boolean,
    onIntent: (ModelsIntent) -> Unit,
) {
    when (entry.status) {
        is ModelStatus.Downloading -> IconButton(
            onClick = { onIntent(ModelsIntent.CancelClicked(entry.model.id)) },
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cancel download")
        }

        ModelStatus.Verifying -> Unit

        is ModelStatus.Downloaded -> Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = { onIntent(ModelsIntent.SelectClicked(entry.model.id)) },
            )
            IconButton(onClick = { onIntent(ModelsIntent.DeleteRequested(entry.model.id)) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete model",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        else -> FilledTonalButton(
            onClick = { onIntent(ModelsIntent.DownloadClicked(entry.model.id)) },
        ) {
            Text("Download")
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0) "$bytes B" else "%.1f %s".format(value, units[unit])
}

@Preview(showBackground = true)
@Composable
private fun ModelsScreenPreview() {
    MishtiTheme {
        ModelsScreen(
            state = ModelsUiState(
                selectedModelId = ModelCatalog.models[0].id,
                entries = listOf(
                    ModelEntry(ModelCatalog.models[0], ModelStatus.Downloaded(270_590_880L)),
                    ModelEntry(
                        ModelCatalog.models[1],
                        ModelStatus.Downloading(120_000_000L, 397_808_192L),
                    ),
                    ModelEntry(ModelCatalog.models[2], ModelStatus.NotDownloaded),
                ),
                storage = StorageUsage(270_590_880L, 19_000_000_000L),
                capability = DeviceCapability.Supported(3_879_952_000L),
            ),
            onIntent = {},
        )
    }
}

