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

fun EntryProviderScope<AppRouteKey>.settingsEntry(navigator: Navigator) {
    entry<AppRouteKey.Settings> {
        SettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.Document> { key ->
        DocumentRoute(document = key.document, navigator = navigator)
    }
    entry<AppRouteKey.AppearanceSettings> {
        AppearanceSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.ChatSettings> {
        ChatSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.GenerationSettings> {
        GenerationSettingsRoute(navigator = navigator)
    }
    entry<AppRouteKey.PerformanceSettings> {
        PerformanceSettingsRoute(navigator = navigator)
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
