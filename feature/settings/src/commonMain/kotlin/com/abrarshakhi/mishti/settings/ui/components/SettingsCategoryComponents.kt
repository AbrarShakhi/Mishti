package com.abrarshakhi.mishti.settings.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.ui.components.ShapedIcon
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_failed
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_idle
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_loading
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_ready
import com.abrarshakhi.mishti.feature.settings.resources.settings_opens_browser
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CategoryRow(
    shapes: ListItemShapes,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    supporting: String,
    onClick: () -> Unit,
    external: Boolean = false,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        leadingContent = { ShapedIcon(imageVector = icon, containerColor = iconColor) },
        trailingContent = { if (external) ExternalLinkIcon() },
        supportingContent = { Text(supporting) },
    ) {
        Text(title)
    }
}

@Composable
internal fun InternalLinkIcon() {
    Icon(MaterialTheme.icons.navigation.arrowOut.filled, contentDescription = null)
}

@Composable
internal fun ExternalLinkIcon() {
    Icon(
        MaterialTheme.icons.navigation.browser.filled,
        contentDescription = stringResource(FeatureSettingsRes.string.settings_opens_browser),
    )
}

@Composable
internal fun modelSummary(engineState: EngineState): String = when (engineState) {
    is EngineState.Ready -> stringResource(
        FeatureSettingsRes.string.settings_model_ready,
        engineState.model.name,
    )

    is EngineState.Loading -> stringResource(
        FeatureSettingsRes.string.settings_model_loading,
        engineState.model.name,
    )

    is EngineState.Failed -> stringResource(FeatureSettingsRes.string.settings_model_failed)

    EngineState.Idle -> stringResource(FeatureSettingsRes.string.settings_model_idle)
}
