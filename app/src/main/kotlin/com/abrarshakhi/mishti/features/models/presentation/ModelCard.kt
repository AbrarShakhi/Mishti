package com.abrarshakhi.mishti.features.models.presentation

import android.text.format.Formatter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.model.ModelStatus

/** A byte count the way Android's own Settings shows it, localised. */
@Composable
internal fun formatSize(bytes: Long): String =
    Formatter.formatShortFileSize(LocalContext.current, bytes)

@Composable
internal fun ModelCard(
    entry: ModelEntry,
    isSelected: Boolean,
    deviceMemoryBytes: Long?,
    onIntent: (ModelsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = entry.model
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "ModelCardContainer",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(Spacing.Large)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModelGlyph()
                Spacer(Modifier.width(Spacing.Large))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${model.parameters} · ${model.quantization} · " +
                            formatSize(model.sizeBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isSelected) InUseBadge()
            }

            Spacer(Modifier.height(Spacing.Medium))
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(Spacing.Small))
            MemoryRequirement(required = model.minRamBytes, available = deviceMemoryBytes)

            Spacer(Modifier.height(Spacing.Medium))
            StatusArea(entry = entry, isSelected = isSelected, onIntent = onIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModelGlyph() {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = MaterialShapes.Cookie4Sided.toShape(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Memory, contentDescription = null)
        }
    }
}

@Composable
private fun InUseBadge() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.Small, end = Spacing.Medium, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("In use", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** How much memory the model wants, called out in red when this phone has less. */
@Composable
private fun MemoryRequirement(required: Long, available: Long?) {
    val tooLittle = available != null && available < required
    Text(
        text = if (tooLittle) {
            "Needs ${formatSize(required)} of memory, more than this phone has"
        } else {
            "Needs ${formatSize(required)} of memory"
        },
        style = MaterialTheme.typography.labelLarge,
        color = if (tooLittle) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

private enum class StatusKind { Download, Downloading, Verifying, Ready, Failed }

private fun ModelStatus.kind() = when (this) {
    ModelStatus.NotDownloaded -> StatusKind.Download
    is ModelStatus.Downloading -> StatusKind.Downloading
    ModelStatus.Verifying -> StatusKind.Verifying
    is ModelStatus.Downloaded -> StatusKind.Ready
    is ModelStatus.Failed -> StatusKind.Failed
}

@Composable
private fun StatusArea(
    entry: ModelEntry,
    isSelected: Boolean,
    onIntent: (ModelsIntent) -> Unit,
) {
    val id = entry.model.id
    val enterSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exitSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = entry.status.kind(),
        transitionSpec = { fadeIn(enterSpec) togetherWith fadeOut(exitSpec) },
        label = "ModelStatus",
    ) { kind ->
        when (kind) {
            StatusKind.Download -> TrailingActions {
                TonalButton(
                    text = "Download",
                    icon = Icons.Filled.Download,
                    onClick = { onIntent(ModelsIntent.DownloadClicked(id)) },
                )
            }

            StatusKind.Downloading -> DownloadProgress(
                status = entry.status as? ModelStatus.Downloading,
                onCancel = { onIntent(ModelsIntent.CancelClicked(id)) },
            )

            StatusKind.Verifying -> VerifyingRow()

            StatusKind.Ready -> TrailingActions {
                TextButton(onClick = { onIntent(ModelsIntent.DeleteRequested(id)) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                if (!isSelected) {
                    Button(onClick = { onIntent(ModelsIntent.SelectClicked(id)) }) {
                        Text("Use")
                    }
                }
            }

            StatusKind.Failed -> Column {
                Text(
                    text = (entry.status as? ModelStatus.Failed)?.reason.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(Spacing.Small))
                TrailingActions {
                    TonalButton(
                        text = "Try again",
                        icon = Icons.Filled.Refresh,
                        onClick = { onIntent(ModelsIntent.DownloadClicked(id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailingActions(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun TonalButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DownloadProgress(status: ModelStatus.Downloading?, onCancel: () -> Unit) {
    val fraction = status?.fraction ?: 0f
    val progress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "DownloadProgress",
    )

    Column {
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (status != null) {
                    "${formatSize(status.downloadedBytes)} of ${formatSize(status.totalBytes)}"
                } else {
                    "Starting…"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VerifyingRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        LoadingIndicator(modifier = Modifier.size(32.dp))
        Text(
            text = "Checking the download…",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
