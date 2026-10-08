package com.abrarshakhi.mishti.features.models.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.abrarshakhi.mishti.common.device.DeviceCapability
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.features.models.domain.model.Catalog
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.memoryFit
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import kotlinx.coroutines.launch

internal const val ShelfPage = 0
internal const val BrowsePage = 1

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelsScreen(
    state: ModelsUiState,
    pagerState: PagerState,
    onIntent: (ModelsIntent) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val background = MaterialTheme.colorScheme.surfaceContainer
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Mistir Bhandar") },
                subtitle = {
                    Text(
                        "${formatSize(state.storage.usedBytes)} on your shelf · " +
                            "${formatSize(state.storage.availableBytes)} free",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onImport, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Import a model file")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = background,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage, matchContentSize = true),
                        width = Dp.Unspecified,
                    )
                },
            ) {
                Tab(
                    selected = pagerState.currentPage == ShelfPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(ShelfPage) } },
                    text = { Text("My shelf") },
                    icon = {
                        val active = state.transfers.count { it.isActive }
                        BadgedBox(badge = { if (active > 0) Badge { Text("$active") } }) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null)
                        }
                    },
                )
                Tab(
                    selected = pagerState.currentPage == BrowsePage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(BrowsePage) } },
                    text = { Text("Browse") },
                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                when (page) {
                    ShelfPage -> ShelfTab(
                        state = state,
                        onIntent = onIntent,
                        onBrowse = { scope.launch { pagerState.animateScrollToPage(BrowsePage) } },
                        onImport = onImport,
                    )
                    else -> BrowseTab(state = state, onIntent = onIntent)
                }
            }
        }
    }

    state.details?.let { model ->
        val item = state.browse.let { content ->
            (content.recommended + content.sections.flatMap { it.items }).find { it.model.id == model.id }
        } ?: CatalogItem(
            model = model,
            fit = memoryFit(
                model.requiredRamBytes,
                state.capability.totalMemoryBytes,
            ),
            isOnShelf = state.shelf.any { it.id == model.id },
            transfer = state.transfers.find { it.id == model.id }?.status,
        )
        ModelDetailsSheet(
            item = item,
            deviceRamBytes = state.capability.totalMemoryBytes,
            onDownload = { onIntent(ModelsIntent.DownloadClicked(model)) },
            onCancel = { onIntent(ModelsIntent.CancelClicked(model.id)) },
            onDismiss = { onIntent(ModelsIntent.DetailsDismissed) },
        )
    }

    state.deleting?.let { model ->
        AlertDialog(
            onDismissRequest = { onIntent(ModelsIntent.DeleteCancelled) },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Delete ${model.name}?") },
            text = {
                Text(
                    if (model.origin == ModelOrigin.Imported) {
                        "The copy on Mishti's shelf will be removed. Your original file is not touched."
                    } else {
                        "The file will be removed from this phone. You can download it again later."
                    },
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

private val previewModels = listOf(
    CatalogModel(
        id = "qwen3-0.6b-q4km", source = CatalogSource.MistirBhandar, name = "Qwen3 0.6B",
        description = "Newer and sharper at the same tiny size.", publisher = "Qwen",
        parametersLabel = "752M", quantization = "Q4_K_M", hfRepo = "bartowski/Qwen_Qwen3-0.6B-GGUF",
        hfFile = "Qwen_Qwen3-0.6B-Q4_K_M.gguf", sizeBytes = 484_220_320L, sha256 = "",
        requiredRamBytes = 3_526_330_480L, contextLength = 32768, license = "Apache 2.0",
        tags = setOf("recommended"), groupId = null,
    ),
    CatalogModel(
        id = "gemma-3-4b-it-q4km", source = CatalogSource.MistirBhandar, name = "Gemma 3 4B",
        description = null, publisher = "Google", parametersLabel = "3.9B", quantization = "Q4_K_M",
        hfRepo = "ggml-org/gemma-3-4b-it-GGUF", hfFile = "gemma-3-4b-it-Q4_K_M.gguf",
        sizeBytes = 2_489_757_856L, sha256 = "", requiredRamBytes = 6_534_636_784L,
        contextLength = 131072, license = null, tags = emptySet(), groupId = null,
    ),
)

@Preview(showBackground = true)
@Composable
private fun ModelsScreenPreview() {
    MishtiTheme {
        ModelsScreen(
            state = ModelsUiState(
                isLoading = false,
                shelf = listOf(
                    ShelfModel(
                        "smollm2", "SmolLM2 360M", ModelOrigin.MistirBhandar, "Q4_K_M", "362M",
                        270_590_880L, null, null, "llama", 8192, null, 0L,
                    ),
                ),
                selectedModelId = "smollm2",
                storage = StorageUsage(270_590_880L, 19_000_000_000L),
                capability = DeviceCapability.Supported(5_800_000_000L),
                catalogs = mapOf(
                    CatalogSource.MistirBhandar to CatalogState.Ready(
                        Catalog(CatalogSource.MistirBhandar, "1", previewModels, recommendedIds = listOf("qwen3-0.6b-q4km")),
                        updatedAtMillis = null,
                        isOffline = false,
                        isRefreshing = false,
                    ),
                ),
            ),
            pagerState = rememberPagerState(initialPage = BrowsePage) { 2 },
            onIntent = {},
            onImport = {},
            onBack = {},
        )
    }
}
