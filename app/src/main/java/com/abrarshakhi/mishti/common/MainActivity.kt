package com.abrarshakhi.mishti.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.mishti.common.main.AppRoot
import com.abrarshakhi.mishti.common.main.MainAppViewModel
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainAppViewModel: MainAppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { mainAppViewModel.startRoute.value == null }
        enableEdgeToEdge()
        setContent {
            val themeSettings by mainAppViewModel.themeSettings.collectAsStateWithLifecycle()
            MishtiTheme(settings = themeSettings) {
                val startRoute by mainAppViewModel.startRoute.collectAsStateWithLifecycle()
                startRoute?.let { route ->
                    AppRoot(startRoute = route, mainAppViewModel = mainAppViewModel)
                }
            }
        }
    }
}