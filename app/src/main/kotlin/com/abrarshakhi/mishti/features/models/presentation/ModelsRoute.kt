package com.abrarshakhi.mishti.features.models.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.mvi.CollectEffects
import org.koin.androidx.compose.koinViewModel

@Composable
fun ModelsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ModelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = ShelfPage) { 2 }
    var openedOnBrowse by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !openedOnBrowse && state.shelf.isEmpty() && state.transfers.isEmpty()) {
            openedOnBrowse = true
            pagerState.scrollToPage(BrowsePage)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    val needsNotificationPermission = remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun askForNotifications() {
        if (needsNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val pickFile =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) viewModel.onIntent(ModelsIntent.ImportPicked(uri.toString()))
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
            if (intent is ModelsIntent.DownloadClicked) askForNotifications()
            viewModel.onIntent(intent)
        },
        onImport = {
            askForNotifications()
            pickFile.launch(arrayOf("application/octet-stream", "*/*"))
        },
        onBack = onBack,
        modifier = modifier,
    )
}
