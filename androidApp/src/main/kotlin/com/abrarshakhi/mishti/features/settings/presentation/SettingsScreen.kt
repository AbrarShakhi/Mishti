package com.abrarshakhi.mishti.features.settings.presentation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.ShapedIcon
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.icons

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
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup(title = stringResource(R.string.settings_section_model)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.store.filled,
                        iconColor = modelColor,
                        title = stringResource(R.string.models_title),
                        supporting = modelSummary(state.engineState),
                        onClick = onOpenModels,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.settings_section_general)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.theme.filled,
                        iconColor = generalColor,
                        title = stringResource(R.string.settings_appearance),
                        supporting = stringResource(R.string.settings_appearance_subtitle),
                        onClick = onOpenAppearance,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.chat.filled,
                        iconColor = generalColor,
                        title = stringResource(R.string.settings_chat),
                        supporting = stringResource(R.string.settings_chat_subtitle),
                        onClick = onOpenChat,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.settings_section_advanced)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.config.filled,
                        iconColor = advancedColor,
                        title = stringResource(R.string.settings_generation),
                        supporting = stringResource(R.string.settings_generation_subtitle),
                        onClick = onOpenGeneration,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.speed.filled,
                        iconColor = advancedColor,
                        title = stringResource(R.string.settings_performance),
                        supporting = stringResource(R.string.settings_performance_subtitle),
                        onClick = onOpenPerformance,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.settings_section_about)) {
                row { shapes ->
                    SegmentedListItem(
                        onClick = { onOpenDocument(AppDocument.About) },
                        shapes = shapes,
                        leadingContent = { AppMark() },
                        trailingContent = { InternalLinkIcon() },
                        supportingContent = {
                            Text(stringResource(R.string.settings_version, versionName))
                        },
                    ) {
                        Text(stringResource(R.string.document_about))
                    }
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.favorite.filled,
                        iconColor = aboutColor,
                        title = stringResource(R.string.document_credits),
                        supporting = stringResource(R.string.settings_credits_subtitle),
                        onClick = { onOpenDocument(AppDocument.Credits) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.privacy.filled,
                        iconColor = aboutColor,
                        title = stringResource(R.string.document_privacy),
                        supporting = stringResource(R.string.settings_privacy_subtitle),
                        onClick = { onOpenDocument(AppDocument.Privacy) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.gavel.filled,
                        iconColor = aboutColor,
                        title = stringResource(R.string.document_terms),
                        supporting = stringResource(R.string.settings_terms_subtitle),
                        onClick = { onOpenDocument(AppDocument.Terms) },
                    )
                }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.settings_section_open_source)) {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.github.filled,
                        iconColor = communityColor,
                        title = stringResource(R.string.settings_source_code),
                        supporting = stringResource(R.string.settings_source_code_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.REPOSITORY) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.visuals.error.filled,
                        iconColor = communityColor,
                        title = stringResource(R.string.settings_report_issue),
                        supporting = stringResource(R.string.settings_report_issue_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.REPORT_ISSUE) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.actions.lightTheme.filled,
                        iconColor = communityColor,
                        title = stringResource(R.string.settings_suggest_model),
                        supporting = stringResource(R.string.settings_suggest_model_subtitle),
                        external = true,
                        onClick = { uriHandler.openUri(ProjectLinks.SUGGEST_MODEL) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.volunteerActivism.filled,
                        iconColor = communityColor,
                        title = stringResource(R.string.document_contributing),
                        supporting = stringResource(R.string.settings_contribute_subtitle),
                        onClick = { onOpenDocument(AppDocument.Contributing) },
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = MaterialTheme.icons.navigation.star.filled,
                        iconColor = communityColor,
                        title = stringResource(R.string.settings_star),
                        supporting = stringResource(R.string.settings_star_subtitle),
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
        contentDescription = stringResource(R.string.settings_opens_browser),
    )
}

@Composable
private fun modelSummary(engineState: EngineState): String = when (engineState) {
    is EngineState.Ready -> stringResource(R.string.settings_model_ready, engineState.model.name)

    is EngineState.Loading -> stringResource(
        R.string.settings_model_loading,
        engineState.model.name,
    )

    is EngineState.Failed -> stringResource(R.string.settings_model_failed)

    EngineState.Idle -> stringResource(R.string.settings_model_idle)
}

@Preview(showBackground = true)
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
