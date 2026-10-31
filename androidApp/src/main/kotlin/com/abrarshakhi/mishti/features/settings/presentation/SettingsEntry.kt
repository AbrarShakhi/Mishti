package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.abrarshakhi.mishti.BuildConfig
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.navigation.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

fun EntryProviderScope<AppRouteKey>.settingsEntry() {
    entry<AppRouteKey.Settings> {
        SettingsRoute(navigator = koinInject())
    }
    entry<AppRouteKey.Document> { key ->
        DocumentRoute(document = key.document, navigator = koinInject())
    }
    entry<AppRouteKey.AppearanceSettings> {
        AppearanceSettingsRoute(navigator = koinInject())
    }
    entry<AppRouteKey.ChatSettings> {
        ChatSettingsRoute(navigator = koinInject())
    }
    entry<AppRouteKey.GenerationSettings> {
        GenerationSettingsRoute(navigator = koinInject())
    }
    entry<AppRouteKey.PerformanceSettings> {
        PerformanceSettingsRoute(navigator = koinInject())
    }
}

@Composable
private fun SettingsRoute(navigator: Navigator) {
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
private fun AppearanceSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
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
private fun ChatSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
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
private fun GenerationSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
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
private fun PerformanceSettingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    PerformanceSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}
