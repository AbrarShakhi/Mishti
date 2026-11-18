package com.abrarshakhi.mishti.models.ui

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.core.ui.components.CollectEffects
import com.abrarshakhi.mishti.core.ui.utils.DrawerController
import com.abrarshakhi.mishti.core.ui.utils.Navigator
import com.abrarshakhi.mishti.models.ui.components.rememberModelFilePicker
import com.abrarshakhi.mishti.models.ui.components.rememberTransferNotificationRequest
import com.abrarshakhi.mishti.models.ui.utils.BrowsePage
import com.abrarshakhi.mishti.models.ui.utils.ShelfPage
import com.abrarshakhi.mishti.models.ui.viewmodels.ModelsViewModel
import com.abrarshakhi.mishti.models.ui.viewmodels.contracts.ModelsEffect
import com.abrarshakhi.mishti.models.ui.viewmodels.contracts.ModelsIntent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelsRoute() {
    val navigator: Navigator = koinInject()
    val drawerController: DrawerController = koinInject()

    val viewModel: ModelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = ShelfPage) { 2 }
    var initialPageChosen by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !initialPageChosen) {
            initialPageChosen = true
            if (state.shelf.isEmpty() && state.transfers.isEmpty()) {
                pagerState.scrollToPage(
                    BrowsePage,
                )
            }
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
        onOpenDrawer = { coroutineScope.launch { drawerController.open() } },
    )
}
