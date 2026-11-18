@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.settings.ui

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
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.designsystem.theme.fontFamily
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.designsystem.theme.isDark
import com.abrarshakhi.mishti.core.designsystem.theme.rememberThemeColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppColorScheme
import com.abrarshakhi.mishti.core.domain.theme.AppFont
import com.abrarshakhi.mishti.core.domain.theme.ThemeMode
import com.abrarshakhi.mishti.feature.settings.resources.settings_appearance
import com.abrarshakhi.mishti.feature.settings.resources.settings_appearance_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_colour
import com.abrarshakhi.mishti.feature.settings.resources.settings_colour_dynamic_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_theme
import com.abrarshakhi.mishti.feature.settings.resources.settings_typeface
import com.abrarshakhi.mishti.feature.settings.resources.settings_typeface_hint
import com.abrarshakhi.mishti.feature.settings.resources.settings_typeface_sample
import com.abrarshakhi.mishti.settings.ui.components.FontSelector
import com.abrarshakhi.mishti.settings.ui.components.PaletteRow
import com.abrarshakhi.mishti.settings.ui.components.SettingsGroup
import com.abrarshakhi.mishti.settings.ui.components.SettingsPanel
import com.abrarshakhi.mishti.settings.ui.components.SettingsScaffold
import com.abrarshakhi.mishti.settings.ui.components.ThemeModeSelector
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.SettingsIntent
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.SettingsUiState
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

@Composable
fun AppearanceSettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = state.theme

    SettingsScaffold(
        title = stringResource(FeatureSettingsRes.string.settings_appearance),
        subtitle = stringResource(FeatureSettingsRes.string.settings_appearance_subtitle),
        onNavigate = onBack,
        isRootSetting = false,
        modifier = modifier,
    ) {
        item {
            SettingsGroup {
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = stringResource(FeatureSettingsRes.string.settings_theme),
                    ) {
                        ThemeModeSelector(
                            selected = theme.mode,
                            onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                        )
                    }
                }
                row { shapes ->
                    SettingsPanel(
                        shapes = shapes,
                        title = stringResource(FeatureSettingsRes.string.settings_colour),
                        supporting = if (AppColorScheme.Dynamic in state.colorSchemes) {
                            stringResource(FeatureSettingsRes.string.settings_colour_dynamic_hint)
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
                        title = stringResource(FeatureSettingsRes.string.settings_typeface),
                        supporting = stringResource(FeatureSettingsRes.string.settings_typeface_hint),
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

@Preview
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
