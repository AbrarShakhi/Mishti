package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelDetailsSheet(
    item: CatalogItem,
    deviceRamBytes: Long?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val model = item.model
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val spin = remember { Animatable(-90f) }
    val spatial = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(model.id) { spin.animateTo(0f, spatial) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacing.ExtraLarge,
                    end = Spacing.ExtraLarge,
                    bottom = Spacing.ExtraLarge
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModelGlyph(name = model.name, seed = model.id, size = 72.dp, rotation = spin.value)
                Spacer(Modifier.width(Spacing.Large))
                Column {
                    Text(model.name, style = MaterialTheme.typography.headlineSmallEmphasized)
                    Text(
                        text = listOfNotNull(
                            model.publisher,
                            stringResource(model.source.labelRes)
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.Large))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            ) {
                model.parametersLabel?.let {
                    InfoPill(
                        stringResource(
                            R.string.models_parameters,
                            it
                        )
                    )
                }
                if (model.quantization.isNotBlank()) InfoPill(model.quantization)
                InfoPill(formatSize(model.sizeBytes))
                model.contextLength?.let {
                    InfoPill(
                        stringResource(
                            R.string.models_context,
                            it / 1024
                        )
                    )
                }
                FitBadge(item.fit)
            }

            val description = model.description ?: model.typicalTokensPerSecond?.let {
                pluralStringResource(
                    R.plurals.models_typical_speed,
                    it.roundToInt(),
                    it.roundToInt()
                )
            }
            description?.let {
                Spacer(Modifier.height(Spacing.Large))
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(Spacing.Large))
            Text(
                text = memoryLine(model.requiredRamBytes, deviceRamBytes, item.fit),
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.fit == MemoryFit.TooBig) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            model.license?.let {
                Text(
                    text = stringResource(R.string.models_licence, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = { uriHandler.openUri(model.pageUrl) },
                modifier = Modifier.padding(top = Spacing.ExtraSmall),
            ) {
                Text(stringResource(R.string.models_view_on_hugging_face))
                Spacer(Modifier.width(Spacing.ExtraSmall))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
            }

            Spacer(Modifier.height(Spacing.Large))
            WideTransferAction(item = item, onDownload = onDownload, onCancel = onCancel)
        }
    }
}

@Composable
private fun memoryLine(required: Long, device: Long?, fit: MemoryFit): String {
    if (device == null) return stringResource(R.string.models_memory_needs, gigabytes(required))
    val id = when (fit) {
        MemoryFit.TooBig -> R.string.models_memory_too_big
        MemoryFit.Tight -> R.string.models_memory_tight
        else -> R.string.models_memory_fits
    }
    return stringResource(id, gigabytes(required), gigabytes(device))
}
