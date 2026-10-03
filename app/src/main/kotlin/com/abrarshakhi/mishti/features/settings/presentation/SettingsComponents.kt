package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.Spacing

/**
 * A titled group of rows drawn as a Material 3 Expressive segmented list: the rows sit 2dp apart
 * and the group as a whole gets the large outer corners.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: SettingsGroupScope.() -> Unit,
) {
    val rows = SettingsGroupScope().apply(content).rows

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = Spacing.Large, bottom = Spacing.Small)
                .semantics { heading() },
        )
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            rows.forEachIndexed { index, row ->
                row(ListItemDefaults.segmentedShapes(index = index, count = rows.size))
            }
        }
    }
}

internal class SettingsGroupScope {
    internal val rows = mutableListOf<@Composable (shapes: ListItemShapes) -> Unit>()

    /** Adds a row; [content] draws it with the [ListItemShapes] for its place in the group. */
    fun row(content: @Composable (shapes: ListItemShapes) -> Unit) {
        rows += content
    }
}

/** A row that isn't a list item (a slider, a picker), dressed to sit in a [SettingsGroup]. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SegmentedCard(
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.Large),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.shape,
        color = ListItemDefaults.segmentedColors().containerColor,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** A row's headline and optional supporting line, styled like a list item's. */
@Composable
internal fun SettingTitle(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (supporting != null) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The tinted circle behind a row's leading icon. */
@Composable
internal fun SettingIcon(imageVector: ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = imageVector, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}
