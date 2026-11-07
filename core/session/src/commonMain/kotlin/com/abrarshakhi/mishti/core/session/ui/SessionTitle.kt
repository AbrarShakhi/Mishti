package com.abrarshakhi.mishti.core.session.ui

import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.core.domain.chat.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.core.session.resources.chat_untitled
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.session.resources.Res as CoreSessionRes

@Composable
fun sessionDisplayTitle(title: String): String = if (title == UNTITLED_SESSION) {
    stringResource(CoreSessionRes.string.chat_untitled)
} else {
    title
}
