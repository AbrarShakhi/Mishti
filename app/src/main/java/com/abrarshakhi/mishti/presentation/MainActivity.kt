package com.abrarshakhi.mishti.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abrarshakhi.mishti.presentation.navigation.MishtiNavHost
import com.abrarshakhi.mishti.presentation.theme.MishtiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MishtiTheme { MishtiNavHost() }
        }
    }
}