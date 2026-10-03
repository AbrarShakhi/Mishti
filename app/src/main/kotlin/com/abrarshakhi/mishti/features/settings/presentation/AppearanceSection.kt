package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import com.abrarshakhi.mishti.common.ui.theme.appColorScheme
import com.abrarshakhi.mishti.common.ui.theme.fontFamily
import com.abrarshakhi.mishti.common.ui.theme.isDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SwatchSize = 56.dp
private val SwatchRingGap = 4.dp
private val SwatchRingWidth = 2.dp

internal fun SettingsGroupScope.appearanceRows(
    theme: ThemeSettings,
    colorSchemes: List<AppColorScheme>,
    onIntent: (SettingsIntent) -> Unit,
) {
    row { shapes ->
        SegmentedCard(shapes) {
            SettingTitle("Theme")
            Spacer(Modifier.height(Spacing.Medium))
            ThemeModeSelector(
                selected = theme.mode,
                onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
            )
        }
    }
    row { shapes ->
        PaletteRow(
            shapes = shapes,
            schemes = colorSchemes,
            selected = theme.colorScheme,
            dark = theme.mode.isDark(),
            onSelect = { onIntent(SettingsIntent.ColorSchemeSelected(it)) },
        )
    }
    row { shapes ->
        SegmentedCard(shapes) {
            SettingTitle("Typeface")
            Spacer(Modifier.height(Spacing.Medium))
            FontSelector(
                selected = theme.font,
                onSelect = { onIntent(SettingsIntent.FontSelected(it)) },
            )
        }
    }
}

/** System, Light and Dark as one connected button group. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val modes = ThemeMode.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        modes.forEachIndexed { index, mode ->
            ToggleButton(
                checked = mode == selected,
                onCheckedChange = { onSelect(mode) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(mode.label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PaletteRow(
    shapes: ListItemShapes,
    schemes: List<AppColorScheme>,
    selected: AppColorScheme,
    dark: Boolean,
    onSelect: (AppColorScheme) -> Unit,
) {
    // The swatches scroll edge to edge inside the card, so the card's side padding is applied
    // to the title and to the scrolling row's content instead of to the card.
    SegmentedCard(shapes, contentPadding = PaddingValues(vertical = Spacing.Large)) {
        SettingTitle("Colour", modifier = Modifier.padding(horizontal = Spacing.Large))
        Spacer(Modifier.height(Spacing.Medium))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Spacing.Large)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
        ) {
            schemes.forEach { scheme ->
                PaletteSwatch(
                    scheme = scheme,
                    dark = dark,
                    selected = scheme == selected,
                    onClick = { onSelect(scheme) },
                )
            }
        }
    }
}

/**
 * A palette previewed the way the system wallpaper picker does it: primary across the top,
 * secondary and tertiary below, generated for the mode the app is in now.
 */
@Composable
private fun PaletteSwatch(
    scheme: AppColorScheme,
    dark: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    // Generating five palettes is too much for one frame on a modest phone, so each swatch
    // builds its colours off the main thread and they fade in once ready.
    val context = LocalContext.current
    val colors by produceState<ColorScheme?>(null, scheme, dark) {
        value = withContext(Dispatchers.Default) { appColorScheme(context, scheme, dark) }
    }
    val placeholder = MaterialTheme.colorScheme.surfaceContainerHighest
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val primary by animateColorAsState(colors?.primary ?: placeholder, effects, label = "Primary")
    val secondary by animateColorAsState(colors?.secondary ?: placeholder, effects, label = "Secondary")
    val tertiary by animateColorAsState(colors?.tertiary ?: placeholder, effects, label = "Tertiary")

    val ringColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "SwatchRing",
    )

    // The whole cell, name included, is the touch target.
    Column(
        modifier = Modifier
            .width(SwatchSize + Spacing.Medium)
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = Spacing.ExtraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(SwatchSize)
                .border(SwatchRingWidth, ringColor, CircleShape)
                .padding(SwatchRingGap)
                .clip(CircleShape)
                .drawBehind {
                    drawArc(primary, startAngle = 180f, sweepAngle = 180f, useCenter = true)
                    drawArc(secondary, startAngle = 90f, sweepAngle = 90f, useCenter = true)
                    drawArc(tertiary, startAngle = 0f, sweepAngle = 90f, useCenter = true)
                },
            contentAlignment = Alignment.Center,
        ) {
            SelectedBadge(
                visible = selected,
                containerColor = colors?.primaryContainer ?: MaterialTheme.colorScheme.primaryContainer,
                contentColor = colors?.onPrimaryContainer ?: MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = scheme.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.Small),
        )
    }
}

/** The check that pops onto the chosen swatch, in that palette's own colours. */
@Composable
private fun SelectedBadge(
    visible: Boolean,
    containerColor: Color,
    contentColor: Color,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        Surface(shape = CircleShape, color = containerColor, contentColor = contentColor) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier
                    .padding(Spacing.ExtraSmall)
                    .size(16.dp),
            )
        }
    }
}

/** One chip per typeface, each label set in the face it stands for. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FontSelector(
    selected: AppFont,
    onSelect: (AppFont) -> Unit,
) {
    FlowRow(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        AppFont.entries.forEach { font ->
            val isSelected = font == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(font) },
                label = { Text(font.label, fontFamily = font.fontFamily()) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            )
        }
    }
}
