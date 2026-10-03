package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    versionName: String,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    onNavigateToModels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val background = MaterialTheme.colorScheme.surfaceContainer
    val onInferenceChange: (InferenceSettings) -> Unit = {
        onIntent(SettingsIntent.InferenceChanged(it))
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = background,
                    scrolledContainerColor = background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            contentPadding = PaddingValues(
                start = Spacing.ScreenMargin,
                end = Spacing.ScreenMargin,
                top = Spacing.Small,
                bottom = Spacing.ExtraExtraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge),
        ) {
            item {
                SettingsGroup(title = "Model") {
                    row { shapes ->
                        SegmentedListItem(
                            onClick = onNavigateToModels,
                            shapes = shapes,
                            leadingContent = {
                                SettingIcon(Icons.Filled.Memory)
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            supportingContent = { Text(modelSummary(state.engineState)) },
                        ) {
                            Text("Models")
                        }
                    }
                }
            }
            item {
                SettingsGroup(title = "Appearance") {
                    appearanceRows(state.theme, state.colorSchemes, onIntent)
                }
            }
            item {
                SettingsGroup(title = "Responses") {
                    responseRows(state.inference, onInferenceChange)
                }
            }
            item {
                SettingsGroup(title = "Sampling") {
                    samplingRows(state.inference, onInferenceChange)
                }
            }
            item {
                SettingsGroup(title = "Performance") {
                    performanceRows(state.inference, onInferenceChange)
                }
            }
            item {
                OutlinedButton(
                    onClick = { onIntent(SettingsIntent.InferenceReset) },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.padding(start = Spacing.Small),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Reset to defaults")
                }
            }
            item {
                SettingsGroup(title = "About") {
                    row { shapes ->
                        SegmentedListItem(
                            shapes = shapes,
                            leadingContent = { SettingIcon(Icons.Filled.Info) },
                            supportingContent = { Text(versionName) },
                        ) {
                            Text("Version")
                        }
                    }
                }
            }
        }
    }
}

private fun modelSummary(engineState: EngineState): String = when (engineState) {
    is EngineState.Ready -> "Using ${engineState.model.name}"
    is EngineState.Loading -> "Loading ${engineState.model.name}…"
    is EngineState.Failed -> "The selected model failed to load"
    EngineState.Idle -> "Download and choose a model"
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
            versionName = "1.1 (2)",
            onIntent = {},
            onBack = {},
            onNavigateToModels = {},
        )
    }
}
