package com.abrarshakhi.mishti.features.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.abrarshakhi.mishti.common.main.ScreenChrome
import com.abrarshakhi.mishti.common.navigation.back

@OptIn(ExperimentalMaterial3Api::class)
fun settingsChrome(): ScreenChrome = ScreenChrome(
    title = "Settings",
    topBar = { scope ->
        TopAppBar(
            title = { Text("Settings") },
            scrollBehavior = scope.scrollBehavior,
            navigationIcon = {
                IconButton(onClick = { scope.backStack.back() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )
    },
)
