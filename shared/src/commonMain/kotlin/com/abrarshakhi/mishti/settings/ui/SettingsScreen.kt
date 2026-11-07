@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.abrarshakhi.mishti.settings.ui

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
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.llm.domain.EngineState
import com.abrarshakhi.mishti.core.llm.domain.ModelHandle
import com.abrarshakhi.mishti.core.ui.components.AppMark
import com.abrarshakhi.mishti.core.ui.components.ShapedIcon
import com.abrarshakhi.mishti.settings.domain.model.AppDocument
import com.abrarshakhi.mishti.settings.domain.model.ProjectLinks
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.document_about
import com.abrarshakhi.mishti.shared.resources.document_contributing
import com.abrarshakhi.mishti.shared.resources.document_credits
import com.abrarshakhi.mishti.shared.resources.document_privacy
import com.abrarshakhi.mishti.shared.resources.document_terms
import com.abrarshakhi.mishti.shared.resources.models_title
import com.abrarshakhi.mishti.shared.resources.settings_appearance
import com.abrarshakhi.mishti.shared.resources.settings_appearance_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_chat
import com.abrarshakhi.mishti.shared.resources.settings_chat_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_contribute_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_credits_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_generation
import com.abrarshakhi.mishti.shared.resources.settings_generation_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_model_failed
import com.abrarshakhi.mishti.shared.resources.settings_model_idle
import com.abrarshakhi.mishti.shared.resources.settings_model_loading
import com.abrarshakhi.mishti.shared.resources.settings_model_ready
import com.abrarshakhi.mishti.shared.resources.settings_opens_browser
import com.abrarshakhi.mishti.shared.resources.settings_performance
import com.abrarshakhi.mishti.shared.resources.settings_performance_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_privacy_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_report_issue
import com.abrarshakhi.mishti.shared.resources.settings_report_issue_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_section_about
import com.abrarshakhi.mishti.shared.resources.settings_section_advanced
import com.abrarshakhi.mishti.shared.resources.settings_section_general
import com.abrarshakhi.mishti.shared.resources.settings_section_model
import com.abrarshakhi.mishti.shared.resources.settings_section_open_source
import com.abrarshakhi.mishti.shared.resources.settings_source_code
import com.abrarshakhi.mishti.shared.resources.settings_source_code_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_star
import com.abrarshakhi.mishti.shared.resources.settings_star_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_suggest_model
import com.abrarshakhi.mishti.shared.resources.settings_suggest_model_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_terms_subtitle
import com.abrarshakhi.mishti.shared.resources.settings_title
import com.abrarshakhi.mishti.shared.resources.settings_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    versionName: String,
    onBack: () -> Unit,
    onOpenModels: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenGeneration: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val modelColor = MaterialTheme.colorScheme.primaryContainer
    val generalColor = MaterialTheme.colorScheme.tertiaryContainer
    val advancedColor = MaterialTheme.colorScheme.secondaryContainer
    val aboutColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val communityColor = MaterialTheme.colorScheme.primaryContainer

    SettingsScaffold(
        title = stringResource(Res.string.settings_title),
        subtitle = stringResource(Res.string.settings_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup(title = stringResource(Res.string.settings_section_model)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.store.filled,
                        iconColor = modelColor,
                        title = stringResource(Res.string.models_title),
                        supporting = modelSummary(state.engineState),
                        onClick = onOpenModels,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(Res.string.settings_section_general)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.theme.filled,
                        iconColor = generalColor,
                        title = stringResource(Res.string.settings_appearance),
                        supporting = stringResource(Res.string.settings_appearance_subtitle),
                        onClick = onOpenAppearance,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.chat.filled,
                        iconColor = generalColor,
                        title = stringResource(Res.string.settings_chat),
                        supporting = stringResource(Res.string.settings_chat_subtitle),
                        onClick = onOpenChat,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(Res.string.settings_section_advanced)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.config.filled,
                        iconColor = advancedColor,
                        title = stringResource(Res.string.settings_generation),
                        supporting = stringResource(Res.string.settings_generation_subtitle),
                        onClick = onOpenGeneration,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.speed.filled,
                        iconColor = advancedColor,
                        title = stringResource(Res.string.settings_performance),
                        supporting = stringResource(Res.string.settings_performance_subtitle),
                        onClick = onOpenPerformance,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(Res.string.settings_section_about)) {
                row { shapes ->
                    SegmentedListItem(
                        onClick = { onOpenDocument(AppDocument.About) },
                        shapes = shapes,
                        leadingContent = { AppMark() },
                        trailingContent = { InternalLinkIcon() },
                        supportingContent = {
                            Text(stringResource(Res.string.settings_version, versionName))
                        },
                    ) {
                        Text(stringResource(Res.string.document_about))
                    }
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.favorite.filled,
                        iconColor = aboutColor,
                        title = stringResource(Res.string.document_credits),
                        supporting = stringResource(Res.string.settings_credits_subtitle),
                        onClick = { onOpenDocument(AppDocument.Credits) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.privacy.filled,
                        iconColor = aboutColor,
                        title = stringResource(Res.string.document_privacy),
                        supporting = stringResource(Res.string.settings_privacy_subtitle),
                        onClick = { onOpenDocument(AppDocument.Privacy) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.gavel.filled,
                        iconColor = aboutColor,
                        title = stringResource(Res.string.document_terms),
                        supporting = stringResource(Res.string.settings_terms_subtitle),
                        onClick = { onOpenDocument(AppDocument.Terms) },
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(Res.string.settings_section_open_source)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.github.filled,
                        iconColor = communityColor,
                        title = stringResource(Res.string.settings_source_code),
                        supporting = stringResource(Res.string.settings_source_code_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.REPOSITORY) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.visuals.error.filled,
                        iconColor = communityColor,
                        title = stringResource(Res.string.settings_report_issue),
                        supporting = stringResource(Res.string.settings_report_issue_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.REPORT_ISSUE) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.actions.lightTheme.filled,
                        iconColor = communityColor,
                        title = stringResource(Res.string.settings_suggest_model),
                        supporting = stringResource(Res.string.settings_suggest_model_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.SUGGEST_MODEL) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.volunteerActivism.filled,
                        iconColor = communityColor,
                        title = stringResource(Res.string.document_contributing),
                        supporting = stringResource(Res.string.settings_contribute_subtitle),
                        onClick = { onOpenDocument(AppDocument.Contributing) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.star.filled,
                        iconColor = communityColor,
                        title = stringResource(Res.string.settings_star),
                        supporting = stringResource(Res.string.settings_star_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.REPOSITORY) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
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
        trailingContent = { if (external) ExternalLinkIcon() else InternalLinkIcon() },
        supportingContent = { Text(supporting) },
    ) {
        Text(title)
    }
}

@Composable
private fun InternalLinkIcon() {
    Icon(MaterialTheme.icons.navigation.arrowOut.filled, contentDescription = null)
}

@Composable
private fun ExternalLinkIcon() {
    Icon(
        MaterialTheme.icons.navigation.browser.filled,
        contentDescription = stringResource(Res.string.settings_opens_browser),
    )
}

@Composable
private fun modelSummary(engineState: EngineState): String = when (engineState) {
    is EngineState.Ready -> stringResource(Res.string.settings_model_ready, engineState.model.name)

    is EngineState.Loading -> stringResource(
        Res.string.settings_model_loading,
        engineState.model.name,
    )

    is EngineState.Failed -> stringResource(Res.string.settings_model_failed)

    EngineState.Idle -> stringResource(Res.string.settings_model_idle)
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    MishtiTheme {
        SettingsScreen(
            state = SettingsUiState(
                engineState = EngineState.Ready(
                    ModelHandle("preview", "Qwen2.5 0.5B Instruct", "/tmp/preview.gguf"),
                ),
            ),
            versionName = "1.4 (4)",
            onBack = {},
            onOpenModels = {},
            onOpenAppearance = {},
            onOpenChat = {},
            onOpenGeneration = {},
            onOpenPerformance = {},
            onOpenDocument = {},
        )
    }
}
