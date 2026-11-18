package com.abrarshakhi.mishti.settings.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.core.designsystem.resources.models_title
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.domain.llm.EngineState
import com.abrarshakhi.mishti.core.domain.llm.ModelHandle
import com.abrarshakhi.mishti.core.ui.components.AppMark
import com.abrarshakhi.mishti.core.ui.components.ShapedIcon
import com.abrarshakhi.mishti.feature.settings.resources.document_about
import com.abrarshakhi.mishti.feature.settings.resources.document_contributing
import com.abrarshakhi.mishti.feature.settings.resources.document_credits
import com.abrarshakhi.mishti.feature.settings.resources.document_privacy
import com.abrarshakhi.mishti.feature.settings.resources.document_terms
import com.abrarshakhi.mishti.feature.settings.resources.settings_appearance
import com.abrarshakhi.mishti.feature.settings.resources.settings_appearance_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_chat
import com.abrarshakhi.mishti.feature.settings.resources.settings_chat_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_contribute_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_credits_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation
import com.abrarshakhi.mishti.feature.settings.resources.settings_generation_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_failed
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_idle
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_loading
import com.abrarshakhi.mishti.feature.settings.resources.settings_model_ready
import com.abrarshakhi.mishti.feature.settings.resources.settings_opens_browser
import com.abrarshakhi.mishti.feature.settings.resources.settings_performance
import com.abrarshakhi.mishti.feature.settings.resources.settings_performance_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_privacy_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_report_issue
import com.abrarshakhi.mishti.feature.settings.resources.settings_report_issue_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_section_about
import com.abrarshakhi.mishti.feature.settings.resources.settings_section_advanced
import com.abrarshakhi.mishti.feature.settings.resources.settings_section_general
import com.abrarshakhi.mishti.feature.settings.resources.settings_section_model
import com.abrarshakhi.mishti.feature.settings.resources.settings_section_open_source
import com.abrarshakhi.mishti.feature.settings.resources.settings_source_code
import com.abrarshakhi.mishti.feature.settings.resources.settings_source_code_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_star
import com.abrarshakhi.mishti.feature.settings.resources.settings_star_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_suggest_model
import com.abrarshakhi.mishti.feature.settings.resources.settings_suggest_model_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_terms_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.settings_title
import com.abrarshakhi.mishti.feature.settings.resources.settings_version
import com.abrarshakhi.mishti.settings.domain.model.ProjectLinks
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes
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
