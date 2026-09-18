package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.fontFamily
import com.abrarshakhi.mishti.common.ui.theme.scheme

private val SectionSpacing = 32.dp

internal val SettingsHorizontalPadding = 24.dp

internal val SettingsRowPadding = PaddingValues(
    horizontal = SettingsHorizontalPadding,
    vertical = 12.dp,
)

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onNavigateToModels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(bottom = SectionSpacing),
    ) {
        SettingsSection(title = "Model") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToModels),
                leadingContent = {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                },
                supportingContent = { Text("Download and manage models") },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                contentPadding = SettingsRowPadding,
                content = { Text("Models") },
            )
        }

        SettingsSection(title = "Theme") {
            ThemeModeSelector(
                selected = state.theme.mode,
                onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
            )
        }

        SettingsSection(title = "Colour") {
            ColorSchemeSelector(
                schemes = state.colorSchemes,
                selected = state.theme.colorScheme,
                onSelect = { onIntent(SettingsIntent.ColorSchemeSelected(it)) },
            )
        }

        SettingsSection(title = "Font") {
            FontPickerRow(
                selected = state.theme.font,
                onSelect = { onIntent(SettingsIntent.FontSelected(it)) },
            )
        }

        SettingsSection(title = "Inference") {
            InferenceSection(
                settings = state.inference,
                onChange = { onIntent(SettingsIntent.InferenceChanged(it)) },
                onReset = { onIntent(SettingsIntent.InferenceReset) },
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = SectionSpacing)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = SettingsHorizontalPadding, vertical = 12.dp),
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsHorizontalPadding),
    ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
            ) {
                Text(mode.label)
            }
        }
    }
}

@Composable
private fun FontPickerRow(
    selected: AppFont,
    onSelect: (AppFont) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { picking = true },
        supportingContent = {
            Text(
                text = selected.label,
                fontFamily = selected.fontFamily(),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        contentPadding = SettingsRowPadding,
        content = { Text("Typeface") },
    )

    if (picking) {
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text("Typeface") },
            text = {
                Column {
                    AppFont.entries.forEach { font ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) {
                                    onSelect(font)
                                    picking = false
                                }
                                .padding(vertical = 8.dp),
                        ) {
                            RadioButton(selected = font == selected, onClick = null)
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = font.label,
                                    fontFamily = font.fontFamily(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = "The quick brown fox",
                                    fontFamily = font.fontFamily(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { picking = false }) { Text("Done") }
            },
        )
    }
}

@Composable
private fun ColorSchemeSelector(
    schemes: List<AppColorScheme>,
    selected: AppColorScheme,
    onSelect: (AppColorScheme) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SettingsHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        schemes.forEach { scheme ->
            ColorSwatch(
                scheme = scheme,
                selected = scheme == selected,
                onSelect = { onSelect(scheme) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    scheme: AppColorScheme,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()
    val swatch = if (scheme == AppColorScheme.Dynamic) {
        MaterialTheme.colorScheme.primary
    } else {
        scheme.scheme(dark).primary
    }
    val onSwatch = if (scheme == AppColorScheme.Dynamic) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        scheme.scheme(dark).onPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp),
    ) {
        Surface(
            color = swatch,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape,
                )
                .clickable(role = Role.RadioButton, onClick = onSelect),
        ) {
            if (selected) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = onSwatch,
                    )
                }
            }
        }
        Text(
            text = scheme.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private fun Color.luminanceIsDark(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) < 0.5f

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MishtiTheme {
        SettingsScreen(state = SettingsUiState(), onIntent = {}, onNavigateToModels = {})
    }
}
