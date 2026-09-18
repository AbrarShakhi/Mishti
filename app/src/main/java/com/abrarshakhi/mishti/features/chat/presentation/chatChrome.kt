package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.common.main.ScreenChrome

fun chatChrome(chatId: Int?): ScreenChrome {
    return ScreenChrome(
        title = "Chat",
        topBar = { _, _ -> },
        fab = {},
    )
}