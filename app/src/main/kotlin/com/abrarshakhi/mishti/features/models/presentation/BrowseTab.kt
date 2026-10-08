package com.abrarshakhi.mishti.features.models.presentation

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.components.SectionHeader
import com.abrarshakhi.mishti.common.ui.components.ShapedIcon
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.CatalogState
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BrowseTab(
    state: ModelsUiState,
    onIntent: (ModelsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val catalog = state.activeCatalog
    val content = remember(state) { state.browse }
    val pullState = rememberPullToRefreshState()
    var pulled by remember { mutableStateOf(false) }
    LaunchedEffect(catalog.isRefreshing) { if (!catalog.isRefreshing) pulled = false }
    val seen = remember(state.catalogSource) { mutableSetOf<String>() }

    PullToRefreshBox(
        isRefreshing = pulled && catalog.isRefreshing,
        onRefresh = {
            pulled = true
            onIntent(ModelsIntent.RefreshRequested)
        },
        state = pullState,
        modifier = modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullState,
                isRefreshing = pulled && catalog.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = Spacing.Large, bottom = Spacing.ExtraExtraLarge),
        ) {
            item(key = "source") {
                CatalogSourceSelector(
                    selected = state.catalogSource,
                    onSelect = { onIntent(ModelsIntent.CatalogSourceSelected(it)) },
                    modifier = Modifier.padding(horizontal = Spacing.ScreenMargin),
                )
            }
            item(key = "status") {
                CatalogStatusLine(
                    state = catalog,
                    modifier = Modifier.padding(
                        start = Spacing.ScreenMargin + Spacing.Small,
                        end = Spacing.ScreenMargin,
                        top = Spacing.Small,
                    ),
                )
            }

            when (catalog) {
                is CatalogState.Loading -> item(key = "loading") { CatalogPlaceholder() }
                is CatalogState.Unavailable -> item(key = "unavailable") {
                    CatalogUnavailable(
                        reason = catalog.reason,
                        onRetry = { onIntent(ModelsIntent.RefreshRequested) },
                    )
                }
                is CatalogState.Ready -> readyContent(state, content, seen, onIntent)
            }
        }
    }
}

private fun LazyListScope.readyContent(
    state: ModelsUiState,
    content: BrowseContent,
    seen: MutableSet<String>,
    onIntent: (ModelsIntent) -> Unit,
) {
    item(key = "search") {
        SearchField(
            query = state.query,
            onQueryChange = { onIntent(ModelsIntent.QueryChanged(it)) },
            modifier = Modifier.padding(
                start = Spacing.ScreenMargin,
                end = Spacing.ScreenMargin,
                top = Spacing.Large,
            ),
        )
    }

    if (content.recommended.isNotEmpty()) {
        item(key = "recommended-header") {
            Column(Modifier.padding(top = Spacing.Large).animateItem()) {
                SectionHeader(
                    title = "Recommended for your phone",
                    modifier = Modifier.padding(horizontal = Spacing.ScreenMargin),
                )
            }
        }
        item(key = "recommended-${state.catalogSource}") {
            RecommendedCarousel(
                items = content.recommended,
                onOpen = { onIntent(ModelsIntent.DetailsOpened(it.model)) },
                onDownload = { onIntent(ModelsIntent.DownloadClicked(it.model)) },
                onCancel = { onIntent(ModelsIntent.CancelClicked(it.model.id)) },
                modifier = Modifier.animateItem(),
            )
        }
    }

    if (content.filters.size > 1) {
        item(key = "filters") {
            FilterRow(
                filters = content.filters,
                selected = state.filter,
                onSelect = { onIntent(ModelsIntent.FilterSelected(it)) },
                modifier = Modifier.padding(top = Spacing.Large),
            )
        }
    }

    if (content.sections.isEmpty()) {
        item(key = "no-results") {
            Text(
                text = if (state.query.isBlank()) {
                    "Nothing on this shelf yet."
                } else {
                    "No sweets match “${state.query.trim()}”."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.ExtraLarge).animateItem(),
            )
        }
    }

    content.sections.forEach { section ->
        item(key = "section-${section.id}") {
            Row(
                modifier = Modifier
                    .animateItem()
                    .padding(start = Spacing.ScreenMargin, end = Spacing.ScreenMargin, top = Spacing.Large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(section.title)
                if (section.isDeviceGroup) {
                    InfoPill(
                        label = "Your phone",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
        }
        itemsIndexed(section.items, key = { _, it -> "${section.id}-${it.model.id}" }) { index, item ->
            CatalogRow(
                item = item,
                shapes = ListItemDefaults.segmentedShapes(index, section.items.size),
                onOpen = { onIntent(ModelsIntent.DetailsOpened(item.model)) },
                onDownload = { onIntent(ModelsIntent.DownloadClicked(item.model)) },
                onCancel = { onIntent(ModelsIntent.CancelClicked(item.model.id)) },
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = Spacing.ScreenMargin, vertical = ListItemDefaults.SegmentedGap / 2)
                    .enterOnce(index, seen.add("${section.id}-${item.model.id}")),
            )
        }
    }

    if (state.catalogSource == CatalogSource.PocketPal) {
        item(key = "credit") {
            Text(
                text = "This list is curated and published by the PocketPal AI project.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.ExtraLarge, vertical = Spacing.Large),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogSourceSelector(
    selected: CatalogSource,
    onSelect: (CatalogSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sources = CatalogSource.entries
    val colors = ToggleButtonDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        sources.forEachIndexed { index, source ->
            ToggleButton(
                checked = source == selected,
                onCheckedChange = { onSelect(source) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                colors = colors,
            ) {
                Icon(
                    imageVector = if (source == CatalogSource.MistirBhandar) Icons.Filled.Storefront else Icons.Filled.Public,
                    contentDescription = null,
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                Text(source.label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CatalogStatusLine(state: CatalogState, modifier: Modifier = Modifier) {
    val text = when (state) {
        is CatalogState.Loading -> "Opening the shop…"
        is CatalogState.Unavailable -> if (state.isRefreshing) "Trying again…" else "Couldn't load this list"
        is CatalogState.Ready -> when {
            state.isRefreshing -> "Checking for new sweets…"
            state.isOffline -> "Offline · showing the saved list"
            state.updatedAtMillis == null -> "Built-in list"
            System.currentTimeMillis() - state.updatedAtMillis < DateUtils.MINUTE_IN_MILLIS -> "Updated just now"
            else -> "Updated " + DateUtils.getRelativeTimeSpanString(
                state.updatedAtMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
            ).toString().replaceFirstChar { it.lowercase() }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search sweets") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendedCarousel(
    items: List<CatalogItem>,
    onOpen: (CatalogItem) -> Unit,
    onDownload: (CatalogItem) -> Unit,
    onCancel: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val carouselState = rememberCarouselState { items.size }
    HorizontalUncontainedCarousel(
        state = carouselState,
        itemWidth = 196.dp,
        itemSpacing = Spacing.Small,
        contentPadding = PaddingValues(horizontal = Spacing.ScreenMargin),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.Small),
    ) { index ->
        val item = items[index]
        RecommendedCard(
            item = item,
            onOpen = { onOpen(item) },
            onDownload = { onDownload(item) },
            onCancel = { onCancel(item) },
            modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecommendedCard(
    item: CatalogItem,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(Spacing.Large)) {
            Row(verticalAlignment = Alignment.Top) {
                ModelGlyph(name = item.model.name, seed = item.model.id, size = 56.dp)
                Spacer(Modifier.weight(1f))
                CompactTransferAction(item = item, onDownload = onDownload, onCancel = onCancel)
            }
            Spacer(Modifier.height(Spacing.Medium))
            Text(
                text = item.model.name,
                style = MaterialTheme.typography.titleMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MetaLine(listOf(item.model.parametersLabel, formatSize(item.model.sizeBytes)))
            Spacer(Modifier.height(Spacing.Small))
            FitBadge(item.fit)
        }
    }
}

@Composable
private fun FilterRow(
    filters: List<CatalogFilter>,
    selected: CatalogFilter,
    onSelect: (CatalogFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogRow(
    item: CatalogItem,
    shapes: ListItemShapes,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = item.model
    SegmentedListItem(
        onClick = onOpen,
        shapes = shapes,
        modifier = modifier.alpha(if (item.fit == MemoryFit.TooBig && !item.isOnShelf) 0.6f else 1f),
        leadingContent = { ModelGlyph(name = model.name, seed = model.id) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) {
                MetaLine(listOf(formatSize(model.sizeBytes), model.parametersLabel, model.quantization, model.publisher))
                if (item.fit != MemoryFit.Fits && item.fit != MemoryFit.Unknown) FitBadge(item.fit)
            }
        },
        trailingContent = {
            CompactTransferAction(item = item, onDownload = onDownload, onCancel = onCancel)
        },
    ) {
        Text(model.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogUnavailable(reason: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.ScreenMargin),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShapedIcon(
                imageVector = Icons.Filled.CloudOff,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                size = 56.dp,
            )
            Spacer(Modifier.height(Spacing.Large))
            Text(reason, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Spacing.Large))
            Button(onClick = onRetry, shapes = ButtonDefaults.shapes()) { Text("Try again") }
        }
    }
}

@Composable
private fun CatalogPlaceholder() {
    val bone = MaterialTheme.colorScheme.surfaceContainerHighest
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.Large)
            .shimmer(rememberShimmer(ShimmerBounds.View)),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        repeat(5) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                Row(modifier = Modifier.padding(Spacing.Large), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(bone, CircleShape))
                    Spacer(Modifier.width(Spacing.Large))
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                        Box(Modifier.fillMaxWidth(0.6f).height(14.dp).background(bone, CircleShape))
                        Box(Modifier.fillMaxWidth(0.4f).height(12.dp).background(bone, CircleShape))
                    }
                }
            }
        }
    }
}
