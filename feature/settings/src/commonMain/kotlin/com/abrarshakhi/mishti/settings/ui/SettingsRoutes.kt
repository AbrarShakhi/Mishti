package com.abrarshakhi.mishti.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import com.abrarshakhi.mishti.core.ui.navigation.Screen
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import com.abrarshakhi.mishti.feature.settings.resources.document_link_failed
import com.abrarshakhi.mishti.settings.domain.model.SettingsPlatformInfo
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

@Composable
fun SettingsRoute() {
    val navigator: Navigator = koinInject()
    val info: SettingsPlatformInfo = koinInject()
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        versionName = info.versionName,
        onBack = { navigator.goBack() },
        onOpenModels = { navigator.navigate(Screen.Models) },
        onOpenAppearance = { navigator.navigate(Screen.AppearanceSettings) },
        onOpenChat = { navigator.navigate(Screen.ChatSettings) },
        onOpenGeneration = { navigator.navigate(Screen.GenerationSettings) },
        onOpenPerformance = { navigator.navigate(Screen.PerformanceSettings) },
        onOpenDocument = { navigator.navigate(Screen.Document(it)) },
    )
}

@Composable
fun AppearanceSettingsRoute() = SettingsDetailRoute { state, onIntent, onBack ->
    AppearanceSettingsScreen(state, onIntent, onBack)
}

@Composable
fun ChatSettingsRoute() = SettingsDetailRoute { state, onIntent, onBack ->
    ChatSettingsScreen(state, onIntent, onBack)
}

@Composable
fun GenerationSettingsRoute() = SettingsDetailRoute { state, onIntent, onBack ->
    GenerationSettingsScreen(state, onIntent, onBack)
}

@Composable
fun PerformanceSettingsRoute() = SettingsDetailRoute { state, onIntent, onBack ->
    PerformanceSettingsScreen(state, onIntent, onBack)
}

@Composable
private fun SettingsDetailRoute(content: @Composable (SettingsUiState, (SettingsIntent) -> Unit, () -> Unit) -> Unit) {
    val navigator: Navigator = koinInject()
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    content(state, viewModel::onIntent, { navigator.goBack() })
}

@Composable
fun DocumentRoute(document: AppDocument) {
    val navigator: Navigator = koinInject()
    val browser = LocalUriHandler.current
    val snackBar: SnackBarDispatcher = koinInject()
    val viewModel: DocumentViewModel = koinViewModel { parametersOf(document) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    DocumentScreen(
        document = document,
        state = state,
        onBack = { navigator.goBack() },
        onOpenDocument = { next -> if (next != document) navigator.navigate(Screen.Document(next)) },
        onOpenExternal = { url ->
            try {
                browser.openUri(url)
            } catch (_: Exception) {
                snackBar.showError(FeatureSettingsRes.string.document_link_failed)
            }
        },
        onRetry = { viewModel.onIntent(DocumentIntent.Retry) },
    )
}
