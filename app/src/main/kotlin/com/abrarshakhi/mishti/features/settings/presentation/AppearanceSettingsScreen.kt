package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.appColorScheme
import com.abrarshakhi.mishti.common.ui.theme.fontFamily
import com.abrarshakhi.mishti.common.ui.theme.isDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SwatchSize = 56.dp
private val SwatchRingGap = 4.dp
private val SwatchRingWidth = 2.dp

@Composable
fun AppearanceSettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = state.theme

    SettingsScaffold(
        title = "Appearance",
        subtitle = AppearanceSubtitle,
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                row { shapes ->
                    SettingsPanel(shapes = shapes, title = "Theme") {
                        ThemeModeSelector(
                            selected = theme.mode,
                            onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                        )
                    }
                }
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = "Colour",
                        supporting = if (AppColorScheme.Dynamic in state.colorSchemes) {
                            "Dynamic takes its colours from your wallpaper"
                        } else {
                            null
                        },
                        edgeToEdge = true,
                    ) {
                        PaletteRow(
                            schemes = state.colorSchemes,
                            selected = theme.colorScheme,
                            dark = theme.mode.isDark(),
                            onSelect = { onIntent(SettingsIntent.ColorSchemeSelected(it)) },
                        )
                    }
                }
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = "Typeface",
                        supporting = "Code always uses JetBrains Mono",
                    ) {
                        FontSelector(
                            selected = theme.font,
                            onSelect = { onIntent(SettingsIntent.FontSelected(it)) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val modes = ThemeMode.entries
    val colors = ToggleButtonDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )

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
                colors = colors,
                contentPadding = PaddingValues(
                    horizontal = Spacing.Small,
                    vertical = Spacing.Medium,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.height(Spacing.ExtraSmall))
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val ThemeMode.icon: ImageVector
    get() = when (this) {
        ThemeMode.System -> Icons.Filled.BrightnessAuto
        ThemeMode.Light -> Icons.Filled.LightMode
        ThemeMode.Dark -> Icons.Filled.DarkMode
    }

@Composable
private fun PaletteRow(
    schemes: List<AppColorScheme>,
    selected: AppColorScheme,
    dark: Boolean,
    onSelect: (AppColorScheme) -> Unit,
) {
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

@Composable
private fun PaletteSwatch(
    scheme: AppColorScheme,
    dark: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FontSelector(
    selected: AppFont,
    onSelect: (AppFont) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        AppFont.entries.forEach { font ->
            FontSample(
                font = font,
                selected = font == selected,
                onClick = { onSelect(font) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FontSample(
    font: AppFont,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val family = font.fontFamily()
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = effects,
        label = "FontContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = effects,
        label = "FontContent",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = effects,
        label = "FontBorder",
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier.semantics { role = Role.RadioButton },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Aa",
                fontFamily = family,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = font.label,
                fontFamily = family,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceSettingsScreenPreview() {
    MishtiTheme {
        AppearanceSettingsScreen(
            state = SettingsUiState(),
            onIntent = {},
            onBack = {},
        )
    }
}
