package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.components.SectionHeader
import com.abrarshakhi.mishti.common.ui.components.ShapedIcon
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus

@Composable
internal fun ShelfTab(
    state: ModelsUiState,
    onIntent: (ModelsIntent) -> Unit,
    onBrowse: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inUse = state.inUse
    val others = state.shelf.filter { it.id != inUse?.id }
    val seen = remember { mutableSetOf<String>() }

    if (state.shelf.isEmpty() && state.transfers.isEmpty() && !state.isLoading) {
        EmptyShelf(onBrowse = onBrowse, onImport = onImport, modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.ScreenMargin,
            end = Spacing.ScreenMargin,
            top = Spacing.Large,
            bottom = Spacing.ExtraExtraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        if (state.transfers.isNotEmpty()) {
            item(key = "oven-header") {
                SectionHeader(stringResource(R.string.models_in_the_oven), Modifier.animateItem())
            }
            itemsIndexed(
                state.transfers,
                key = { _, it -> "transfer-${it.id}" },
            ) { index, transfer ->
                TransferRow(
                    transfer = transfer,
                    shapes = ListItemDefaults.segmentedShapes(index, state.transfers.size),
                    onCancel = { onIntent(ModelsIntent.CancelClicked(transfer.id)) },
                    onDismiss = { onIntent(ModelsIntent.TransferDismissed(transfer.id)) },
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "oven-gap") { Spacer(Modifier.height(Spacing.Large)) }
        }

        if (inUse != null) {
            item(key = "in-use-header") {
                SectionHeader(
                    stringResource(R.string.models_in_use),
                    Modifier.animateItem(),
                )
            }
            item(key = "in-use-${inUse.id}") {
                InUseCard(
                    model = inUse,
                    onDelete = { onIntent(ModelsIntent.DeleteRequested(inUse.id)) },
                    modifier = Modifier
                        .animateItem()
                        .enterOnce(0, seen.add("in-use-${inUse.id}")),
                )
            }
            item(key = "in-use-gap") { Spacer(Modifier.height(Spacing.Large)) }
        }

        if (others.isNotEmpty()) {
            item(key = "shelf-header") {
                SectionHeader(
                    stringResource(if (inUse == null) R.string.models_on_shelf else R.string.models_also_on_shelf),
                    Modifier.animateItem(),
                )
            }
            itemsIndexed(others, key = { _, it -> "shelf-${it.id}" }) { index, model ->
                ShelfRow(
                    model = model,
                    shapes = ListItemDefaults.segmentedShapes(index, others.size),
                    onUse = { onIntent(ModelsIntent.UseClicked(model.id)) },
                    onDelete = { onIntent(ModelsIntent.DeleteRequested(model.id)) },
                    modifier = Modifier
                        .animateItem()
                        .enterOnce(index + 1, seen.add("shelf-${model.id}")),
                )
            }
        }
    }
}

@Composable
private fun InUseCard(model: ShelfModel, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.ExtraLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModelGlyph(
                    name = model.name,
                    seed = model.id,
                    size = 64.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                Spacer(Modifier.width(Spacing.Large))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            model.parametersLabel,
                            model.quantization,
                            formatSize(model.sizeBytes),
                        )
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.Medium))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoPill(
                    label = stringResource(R.string.models_chatting_now),
                    containerColor = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(Spacing.ExtraSmall))
                InfoPill(
                    label = stringResource(model.origin.labelRes),
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete, shapes = IconButtonDefaults.shapes()) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.models_delete_named,
                            model.name,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelfRow(
    model: ShelfModel,
    shapes: ListItemShapes,
    onUse: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    SegmentedListItem(
        onClick = onUse,
        shapes = shapes,
        modifier = modifier,
        onLongClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onDelete()
        },
        onLongClickLabel = stringResource(R.string.models_delete_named, model.name),
        leadingContent = { ModelGlyph(name = model.name, seed = model.id) },
        supportingContent = {
            MetaLine(
                listOf(
                    formatSize(model.sizeBytes),
                    model.parametersLabel,
                    model.quantization,
                    stringResource(model.origin.labelRes),
                ),
            )
        },
        trailingContent = {
            FilledTonalButton(onClick = onUse, shapes = ButtonDefaults.shapes()) {
                Text(
                    stringResource(R.string.models_use),
                )
            }
        },
    ) {
        Text(model.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TransferRow(
    transfer: Transfer,
    shapes: ListItemShapes,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = transfer.status
    SegmentedListItem(
        shapes = shapes,
        modifier = modifier,
        leadingContent = {
            if (status is TransferStatus.Failed) {
                ShapedIcon(
                    imageVector = Icons.Filled.ErrorOutline,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            } else {
                ModelGlyph(name = transfer.name, seed = transfer.id)
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                Text(
                    text = when (status) {
                        is TransferStatus.Downloading ->
                            stringResource(
                                R.string.models_downloading,
                                formatSize(status.doneBytes),
                                formatSize(status.totalBytes),
                            )

                        is TransferStatus.Importing -> if (status.totalBytes > 0) {
                            stringResource(
                                R.string.models_importing,
                                formatSize(status.doneBytes),
                                formatSize(status.totalBytes),
                            )
                        } else {
                            stringResource(R.string.models_reading_file)
                        }

                        TransferStatus.Verifying -> stringResource(R.string.models_checking_file)

                        is TransferStatus.Failed -> stringResource(status.error.messageRes)
                    },
                    color = if (status is TransferStatus.Failed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                val fraction = when (status) {
                    is TransferStatus.Downloading -> status.fraction
                    is TransferStatus.Importing -> if (status.totalBytes > 0) status.fraction else null
                    else -> null
                }
                if (status !is TransferStatus.Failed) {
                    if (fraction != null) {
                        val progress by animateFloatAsState(
                            fraction,
                            MaterialTheme.motionScheme.defaultEffectsSpec(),
                            label = "TransferProgress",
                        )
                        LinearWavyProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        trailingContent = {
            IconButton(onClick = if (status is TransferStatus.Failed) onDismiss else onCancel) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(
                        if (status is TransferStatus.Failed) R.string.action_dismiss else R.string.action_cancel,
                    ),
                )
            }
        },
    ) {
        Text(transfer.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyShelf(onBrowse: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    val rotation by rememberInfiniteTransition(label = "EmptyGlyph").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing)),
        label = "EmptyGlyphTurn",
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ModelGlyph(
                name = "M",
                seed = "mishti",
                size = 96.dp,
                rotation = rotation,
                modifier = Modifier.enterOnce(0, true),
            )
            Spacer(Modifier.height(Spacing.ExtraLarge))
            Text(
                text = stringResource(R.string.models_empty_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.Small))
            Text(
                text = stringResource(R.string.models_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.ExtraLarge))
            Button(onClick = onBrowse, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Filled.Storefront, contentDescription = null)
                Spacer(Modifier.width(Spacing.Small))
                Text(stringResource(R.string.models_empty_browse))
            }
            Spacer(Modifier.height(Spacing.Small))
            OutlinedButton(onClick = onImport, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Filled.UploadFile, contentDescription = null)
                Spacer(Modifier.width(Spacing.Small))
                Text(stringResource(R.string.models_empty_import))
            }
        }
    }
}
