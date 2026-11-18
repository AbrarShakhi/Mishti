package com.abrarshakhi.mishti.models.ui.components

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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal expect fun rememberModelFilePicker(onPicked: (String) -> Unit): () -> Unit

@Composable
internal expect fun rememberTransferNotificationRequest(): () -> Unit
