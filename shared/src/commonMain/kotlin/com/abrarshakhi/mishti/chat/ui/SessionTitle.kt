package com.abrarshakhi.mishti.chat.ui

import androidx.compose.runtime.Composable
import com.abrarshakhi.mishti.chat.domain.model.UNTITLED_SESSION
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.chat_untitled
import org.jetbrains.compose.resources.stringResource

@Composable
fun sessionDisplayTitle(title: String): String = if (title == UNTITLED_SESSION) {
    stringResource(Res.string.chat_untitled)
} else {
    title
}
