@file:Suppress("ktlint:standard:function-naming")

package com.abrarshakhi.mishti.app.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.abrarshakhi.mishti.core.di.setupKoin
import com.abrarshakhi.mishti.core.ui.components.App

fun MainViewController() = ComposeUIViewController(configure = { setupKoin() }) {
    App()
}
