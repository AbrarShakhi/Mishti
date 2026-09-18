package com.abrarshakhi.mishti.features.models.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ModelsRoute(modifier: Modifier = Modifier) {
    val viewModel: ModelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    val needsNotificationPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    }

    ModelsScreen(
        state = state,
        onIntent = { intent ->
            if (intent is ModelsIntent.DownloadClicked && needsNotificationPermission) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewModel.onIntent(intent)
        },
        modifier = modifier,
    )
}
