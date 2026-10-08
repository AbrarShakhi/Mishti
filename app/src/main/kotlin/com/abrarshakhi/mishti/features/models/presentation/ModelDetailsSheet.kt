package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
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
                .padding(start = Spacing.ExtraLarge, end = Spacing.ExtraLarge, bottom = Spacing.ExtraLarge),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModelGlyph(name = model.name, seed = model.id, size = 72.dp, rotation = spin.value)
                Spacer(Modifier.width(Spacing.Large))
                Column {
                    Text(model.name, style = MaterialTheme.typography.headlineSmallEmphasized)
                    Text(
                        text = listOfNotNull(model.publisher, model.source.label).joinToString(" · "),
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
                model.parametersLabel?.let { InfoPill("$it parameters") }
                if (model.quantization.isNotBlank()) InfoPill(model.quantization)
                InfoPill(formatSize(model.sizeBytes))
                model.contextLength?.let { InfoPill("${it / 1024}K context") }
                FitBadge(item.fit)
            }

            model.description?.let {
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
                    text = "Licence: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = { uriHandler.openUri(model.pageUrl) },
                modifier = Modifier.padding(top = Spacing.ExtraSmall),
            ) {
                Text("View on Hugging Face")
                Spacer(Modifier.width(Spacing.ExtraSmall))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
            }

            Spacer(Modifier.height(Spacing.Large))
            WideTransferAction(item = item, onDownload = onDownload, onCancel = onCancel)
        }
    }
}

private fun memoryLine(required: Long, device: Long?, fit: MemoryFit): String {
    val needs = "Needs about ${gigabytes(required)} of memory"
    if (device == null) return "$needs."
    val has = "this phone has ${gigabytes(device)}"
    return when (fit) {
        MemoryFit.TooBig -> "$needs, but $has."
        MemoryFit.Tight -> "$needs and $has, so close other apps first."
        else -> "$needs; $has."
    }
}
