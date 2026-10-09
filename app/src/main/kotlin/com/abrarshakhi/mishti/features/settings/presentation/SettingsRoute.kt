package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.BuildConfig
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.Navigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(navigator: Navigator) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        versionName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        onBack = { navigator.goBack() },
        onOpenModels = { navigator.navigate(AppRouteKey.Models) },
        onOpenAppearance = { navigator.navigate(AppRouteKey.AppearanceSettings) },
        onOpenChat = { navigator.navigate(AppRouteKey.ChatSettings) },
        onOpenGeneration = { navigator.navigate(AppRouteKey.GenerationSettings) },
        onOpenPerformance = { navigator.navigate(AppRouteKey.PerformanceSettings) },
        onOpenDocument = { navigator.navigate(AppRouteKey.Document(it)) },
        modifier = Modifier,
    )
}

@Composable
fun AppearanceSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearanceSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}

@Composable
fun ChatSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChatSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}

@Composable
fun GenerationSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GenerationSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}

@Composable
fun PerformanceSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    PerformanceSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}
