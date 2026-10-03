package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.ModelCatalog
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.model.ModelStatus
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelsScreen(
    state: ModelsUiState,
    onIntent: (ModelsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val background = MaterialTheme.colorScheme.surfaceContainer

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Models") },
                subtitle = { Text("They run entirely on this phone") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = background,
                    scrolledContainerColor = background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.ScreenMargin,
                end = Spacing.ScreenMargin,
                top = Spacing.Small,
                bottom = Spacing.ExtraExtraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
        ) {
            if (state.isLoading) {
                item(key = "placeholder") { ModelsPlaceholder() }
                return@LazyColumn
            }

            if (state.capability is DeviceCapability.UnsupportedLowMemory) {
                item(key = "unsupported") { UnsupportedDeviceNotice(state.capability) }
            }

            item(key = "storage") { StorageSummary(state.storage) }

            items(items = state.entries, key = { it.model.id }) { entry ->
                ModelCard(
                    entry = entry,
                    isSelected = entry.model.id == state.selectedModelId,
                    deviceMemoryBytes = state.capability.totalMemoryBytes,
                    onIntent = onIntent,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    state.deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { onIntent(ModelsIntent.DeleteCancelled) },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Delete ${entry.model.name}?") },
            text = {
                Text("The file will be removed from this phone. You can download it again later.")
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
private fun StorageSummary(storage: StorageUsage) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Storage, contentDescription = null)
                }
            }
            Spacer(Modifier.width(Spacing.Large))
            Column {
                Text(
                    text = "${formatSize(storage.usedBytes)} used by models",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${formatSize(storage.availableBytes)} free on this phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Shimmering cards in the shape of the list, until the catalogue and storage are read. */
@Composable
private fun ModelsPlaceholder() {
    val bone = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(
        modifier = Modifier.shimmer(rememberShimmer(ShimmerBounds.View)),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        PlaceholderCard(height = 72.dp) {
            Box(Modifier.size(40.dp).background(bone, CircleShape))
            Spacer(Modifier.width(Spacing.Large))
            PlaceholderLines(bone, listOf(0.7f, 0.5f))
        }
        repeat(3) {
            PlaceholderCard(height = 196.dp) {
                Box(Modifier.size(48.dp).background(bone, MaterialTheme.shapes.large))
                Spacer(Modifier.width(Spacing.Large))
                PlaceholderLines(bone, listOf(0.8f, 0.55f, 0.95f, 0.6f))
            }
        }
    }
}

@Composable
private fun PlaceholderCard(height: Dp, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Row(modifier = Modifier.padding(Spacing.Large)) { content() }
    }
}

@Composable
private fun PlaceholderLines(color: Color, fractions: List<Float>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
        fractions.forEach { fraction ->
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(14.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

@Composable
private fun UnsupportedDeviceNotice(capability: DeviceCapability.UnsupportedLowMemory) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(Spacing.Large)) {
            Icon(Icons.Filled.Warning, contentDescription = null)
            Spacer(Modifier.width(Spacing.Large))
            Column {
                Text("This phone is not supported", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Running a model needs about ${formatSize(capability.requiredMemoryBytes)} " +
                        "of memory, and this phone has ${formatSize(capability.totalMemoryBytes)}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelsScreenPreview() {
    MishtiTheme {
        ModelsScreen(
            state = ModelsUiState(
                isLoading = false,
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
            onBack = {},
        )
    }
}
