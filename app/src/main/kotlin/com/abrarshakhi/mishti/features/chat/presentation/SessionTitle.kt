package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.features.chat.domain.model.UNTITLED_SESSION

@Composable
fun sessionDisplayTitle(title: String): String = if (title ==
    UNTITLED_SESSION
) {
    stringResource(R.string.chat_untitled)
} else {
    title
}
