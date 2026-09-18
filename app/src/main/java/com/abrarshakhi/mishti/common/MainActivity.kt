package com.abrarshakhi.mishti.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abrarshakhi.mishti.common.main.AppRoot
import com.abrarshakhi.mishti.common.main.MainAppViewModel
import com.abrarshakhi.mishti.common.navigation.AppRouteKey
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MishtiTheme {
                val mainAppViewModel: MainAppViewModel = koinViewModel()
                AppRoot(startRoute = AppRouteKey.Chat(), mainAppViewModel)
            }
        }
    }
}