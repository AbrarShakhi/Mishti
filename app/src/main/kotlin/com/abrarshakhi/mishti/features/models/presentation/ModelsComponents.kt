package com.abrarshakhi.mishti.features.models.presentation

import android.text.format.Formatter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun formatSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)

private val GlyphPolygons
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    get() = listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Sunny,
        MaterialShapes.Flower,
        MaterialShapes.Puffy,
        MaterialShapes.SoftBurst,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Bun,
        MaterialShapes.Clover8Leaf,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ModelGlyph(
    name: String,
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    rotation: Float = 0f,
    containerColor: Color? = null,
) {
    val hash = seed.hashCode().absoluteValue
    val polygon = GlyphPolygons[hash % GlyphPolygons.size]
    val container = containerColor ?: when (hash / GlyphPolygons.size % 3) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val shape = polygon.toShape()

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation }
                .background(container, shape),
        )
        Text(
            text = name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
            style = if (size >= 56.dp) {
                MaterialTheme.typography.headlineSmallEmphasized
            } else {
                MaterialTheme.typography.titleMediumEmphasized
            },
            color = contentColorFor(container),
        )
    }
}

@Composable
internal fun FitBadge(fit: MemoryFit, modifier: Modifier = Modifier) {
    val label = fit.labelRes ?: return
    val container = when (fit) {
        MemoryFit.Tight -> MaterialTheme.colorScheme.tertiaryContainer
        MemoryFit.TooBig -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    InfoPill(label = stringResource(label), containerColor = container, modifier = modifier)
}

@Composable
internal fun InfoPill(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColorFor(containerColor),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = 2.dp),
        )
    }
}

private enum class ActionKind { Idle, Downloading, Verifying, Failed, OnShelf }

private fun actionKind(item: CatalogItem): ActionKind = when {
    item.transfer is TransferStatus.Downloading -> ActionKind.Downloading
    item.transfer is TransferStatus.Verifying -> ActionKind.Verifying
    item.isOnShelf -> ActionKind.OnShelf
    item.transfer is TransferStatus.Failed -> ActionKind.Failed
    else -> ActionKind.Idle
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactTransferAction(
    item: CatalogItem,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = actionKind(item),
        modifier = modifier,
        transitionSpec = {
            (scaleIn(spatial, initialScale = 0.6f) + fadeIn(effects)) togetherWith
                (scaleOut(spatial, targetScale = 0.6f) + fadeOut(effects))
        },
        contentAlignment = Alignment.Center,
        label = "TransferAction",
    ) { kind ->
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            when (kind) {
                ActionKind.Idle -> FilledTonalIconButton(
                    onClick = onDownload,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = stringResource(
                            R.string.models_download_named,
                            item.model.name,
                        ),
                    )
                }

                ActionKind.Downloading -> {
                    val fraction = (item.transfer as? TransferStatus.Downloading)?.fraction ?: 0f
                    val progress by animateFloatAsState(
                        fraction,
                        MaterialTheme.motionScheme.defaultEffectsSpec(),
                        label = "DownloadProgress",
                    )
                    CircularWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(40.dp),
                    )
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.models_cancel_download),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                ActionKind.Verifying -> LoadingIndicator(modifier = Modifier.size(40.dp))

                ActionKind.Failed -> FilledTonalIconButton(
                    onClick = onDownload,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(
                            R.string.models_retry_named,
                            item.model.name,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                ActionKind.OnShelf -> Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.models_on_shelf),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
internal fun WideTransferAction(
    item: CatalogItem,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enter = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exit = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = actionKind(item),
        modifier = modifier.fillMaxWidth(),
        transitionSpec = { fadeIn(enter) togetherWith fadeOut(exit) },
        label = "WideTransferAction",
    ) { kind ->
        when (kind) {
            ActionKind.Idle, ActionKind.Failed -> Button(
                onClick = onDownload,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(
                    imageVector = if (kind ==
                        ActionKind.Failed
                    ) {
                        Icons.Filled.Refresh
                    } else {
                        Icons.Filled.Download
                    },
                    contentDescription = null,
                )
                Spacer(Modifier.width(Spacing.Small))
                Text(
                    text = if (kind == ActionKind.Failed) {
                        stringResource(R.string.action_try_again)
                    } else {
                        stringResource(
                            R.string.models_download_size,
                            formatSize(item.model.sizeBytes),
                        )
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            ActionKind.Downloading, ActionKind.Verifying -> {
                val status = item.transfer
                val fraction = (status as? TransferStatus.Downloading)?.fraction ?: 1f
                val progress by animateFloatAsState(
                    fraction,
                    MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "WideProgress",
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (status) {
                                is TransferStatus.Downloading ->
                                    stringResource(
                                        R.string.models_progress,
                                        formatSize(status.doneBytes),
                                        formatSize(status.totalBytes),
                                    )

                                else -> stringResource(R.string.models_checking_file)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (status is TransferStatus.Downloading) {
                            IconButton(onClick = onCancel) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(
                                        R.string.models_cancel_download,
                                    ),
                                )
                            }
                        }
                    }
                    if (status is TransferStatus.Downloading) {
                        LinearWavyProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            ActionKind.OnShelf -> FilledTonalButton(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(Spacing.Small))
                Text(
                    stringResource(R.string.models_on_shelf),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

internal fun Modifier.enterOnce(index: Int, animate: Boolean): Modifier = composed {
    if (!animate) return@composed this
    val progress = remember { Animatable(0f) }
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val lift = with(LocalDensity.current) { 24.dp.toPx() }
    LaunchedEffect(Unit) {
        delay((min(index, 8) * 40L).milliseconds)
        progress.animateTo(1f, spec)
    }
    graphicsLayer {
        val value = progress.value
        alpha = value.coerceIn(0f, 1f)
        translationY = (1f - value) * lift
    }
}

@Composable
internal fun MetaLine(parts: List<String?>, modifier: Modifier = Modifier) {
    Text(
        text = parts.filterNot { it.isNullOrBlank() }.joinToString(" · "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
internal fun PillRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) { content() }
}
