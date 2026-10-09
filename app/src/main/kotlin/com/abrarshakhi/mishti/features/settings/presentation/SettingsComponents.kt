package com.abrarshakhi.mishti.features.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.components.SectionHeader
import com.abrarshakhi.mishti.common.ui.components.ShapedIcon
import com.abrarshakhi.mishti.common.ui.theme.Spacing

@Composable
internal fun SettingsScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val background = MaterialTheme.colorScheme.surfaceContainer

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title) },
                subtitle = { Text(subtitle) },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
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
                .consumeWindowInsets(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            contentPadding = PaddingValues(
                start = Spacing.ScreenMargin,
                end = Spacing.ScreenMargin,
                top = Spacing.Small,
                bottom = Spacing.ExtraExtraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
            content = content,
        )
    }
}

@Composable
internal fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: SettingsGroupScope.() -> Unit,
) {
    val rows = SettingsGroupScope().apply(content).rows

    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) SectionHeader(title)
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            rows.forEachIndexed { index, row ->
                row(ListItemDefaults.segmentedShapes(index = index, count = rows.size))
            }
        }
    }
}

internal class SettingsGroupScope {
    internal val rows = mutableListOf<@Composable (shapes: ListItemShapes) -> Unit>()

    fun row(content: @Composable (shapes: ListItemShapes) -> Unit) {
        rows += content
    }
}

@Composable
internal fun SettingsPanel(
    shapes: ListItemShapes,
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    edgeToEdge: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.shape,
        color = ListItemDefaults.segmentedColors().containerColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    trailing?.invoke()
                }
                if (supporting != null) {
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = if (edgeToEdge) {
                    Modifier
                } else {
                    Modifier.padding(horizontal = Spacing.Large)
                },
                content = content,
            )
        }
    }
}

internal data class SliderSpec(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val value: Float,
    val range: ClosedFloatingPointRange<Float>,
    val display: @Composable (Float) -> String,
    val intent: (Float) -> SettingsIntent,
    val steps: Int = 0,
)

internal fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> =
    first.toFloat()..last.toFloat()

internal fun SettingsGroupScope.sliderRow(spec: SliderSpec, onIntent: (SettingsIntent) -> Unit) {
    row { shapes ->
        SliderPanel(spec = spec, shapes = shapes, onCommit = { onIntent(spec.intent(it)) })
    }
}

@Composable
private fun SliderPanel(
    spec: SliderSpec,
    shapes: ListItemShapes,
    onCommit: (Float) -> Unit,
) {
    val sliderState = remember(spec.value, spec.steps, spec.range) {
        SliderState(value = spec.value, steps = spec.steps, trackRange = spec.range)
    }

    SettingsPanel(
        shapes = shapes,
        title = stringResource(spec.title),
        supporting = stringResource(spec.description),
        trailing = { ValueBadge(spec.display(sliderState.value)) },
    ) {
        Slider(
            state = sliderState,
            onValueChange = { sliderState.value = it },
            onValueChangeFinished = { onCommit(sliderState.value) },
        )
    }
}

@Composable
internal fun ValueBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.ExtraSmall),
        )
    }
}

@Composable
internal fun RestoreDefaultsRow(
    shapes: ListItemShapes,
    defaults: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        enabled = enabled,
        leadingContent = {
            ShapedIcon(
                imageVector = Icons.Filled.RestartAlt,
                containerColor = if (enabled) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                contentColor = if (enabled) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
        supportingContent = {
            Text(if (enabled) defaults else stringResource(R.string.settings_already_defaults))
        },
    ) {
        Text(stringResource(R.string.settings_restore_defaults))
    }
}
