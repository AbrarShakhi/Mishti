package com.abrarshakhi.mishti.models.ui

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.core.ui.mvi.CollectEffects
import com.abrarshakhi.mishti.core.ui.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelsRoute() {
    val navigator: Navigator = koinInject()
    val viewModel: ModelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = ShelfPage) { 2 }
    var initialPageChosen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !initialPageChosen) {
            initialPageChosen = true
            if (state.shelf.isEmpty() && state.transfers.isEmpty()) pagerState.scrollToPage(BrowsePage)
        }
    }
    val requestNotifications = rememberTransferNotificationRequest()
    val importFile = rememberModelFilePicker { uri ->
        viewModel.onIntent(ModelsIntent.ImportPicked(uri))
    }
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            ModelsEffect.ShowShelf -> pagerState.animateScrollToPage(ShelfPage)
        }
    }
    ModelsScreen(
        state = state,
        pagerState = pagerState,
        onIntent = { intent ->
            if (intent is ModelsIntent.DownloadClicked) requestNotifications()
            viewModel.onIntent(intent)
        },
        onImport = {
            requestNotifications()
            importFile()
        },
        onBack = { navigator.goBack() },
    )
}

@Composable
internal expect fun rememberModelFilePicker(onPicked: (String) -> Unit): () -> Unit

@Composable
internal expect fun rememberTransferNotificationRequest(): () -> Unit
