package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.ShapedIcon
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme

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
    modifier: Modifier = Modifier,
) {
    val modelColor = MaterialTheme.colorScheme.primaryContainer
    val generalColor = MaterialTheme.colorScheme.tertiaryContainer
    val advancedColor = MaterialTheme.colorScheme.secondaryContainer

    SettingsScaffold(
        title = "Settings",
        subtitle = "Make Mishti yours",
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            SettingsGroup(title = "Model") {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = Icons.Filled.Storefront,
                        iconColor = modelColor,
                        title = "Mistir Bhandar",
                        supporting = modelSummary(state.engineState),
                        onClick = onOpenModels,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = "General") {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = Icons.Filled.Palette,
                        iconColor = generalColor,
                        title = "Appearance",
                        supporting = AppearanceSubtitle,
                        onClick = onOpenAppearance,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = Icons.AutoMirrored.Filled.Chat,
                        iconColor = generalColor,
                        title = "Chat",
                        supporting = ChatSubtitle,
                        onClick = onOpenChat,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = "Advanced") {
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = Icons.Filled.Tune,
                        iconColor = advancedColor,
                        title = "Generation",
                        supporting = GenerationSubtitle,
                        onClick = onOpenGeneration,
                    )
                }
                row { shapes ->
                    CategoryRow(
                        shapes = shapes,
                        icon = Icons.Filled.Speed,
                        iconColor = advancedColor,
                        title = "Performance",
                        supporting = PerformanceSubtitle,
                        onClick = onOpenPerformance,
                    )
                }
            }
        }
        item {
            SettingsGroup(title = "About") {
                row { shapes ->
                    SegmentedListItem(
                        shapes = shapes,
                        leadingContent = { AppMark() },
                        supportingContent = { Text("Version $versionName") },
                    ) {
                        Text("Mishti")
                    }
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
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        leadingContent = { ShapedIcon(imageVector = icon, containerColor = iconColor) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        supportingContent = { Text(supporting) },
    ) {
        Text(title)
    }
}

private fun modelSummary(engineState: EngineState): String = when (engineState) {
    is EngineState.Ready -> "Using ${engineState.model.name}"
    is EngineState.Loading -> "Loading ${engineState.model.name}…"
    is EngineState.Failed -> "The selected model failed to load"
    EngineState.Idle -> "Pick a sweet to start chatting"
}

internal const val AppearanceSubtitle = "Theme, colour and typeface"
internal const val ChatSubtitle = "Pre-instruction and reply length"
internal const val GenerationSubtitle = "How Mishti picks each word"
internal const val PerformanceSubtitle = "Memory and processor use"

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
            versionName = "1.2 (3)",
            onBack = {},
            onOpenModels = {},
            onOpenAppearance = {},
            onOpenChat = {},
            onOpenGeneration = {},
            onOpenPerformance = {},
        )
    }
}
