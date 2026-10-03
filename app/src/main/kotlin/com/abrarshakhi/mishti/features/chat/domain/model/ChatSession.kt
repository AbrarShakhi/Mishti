package com.abrarshakhi.mishti.features.chat.domain.model

data class ChatSession(
    val id: String,
    val title: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

const val UNTITLED_SESSION = "New chat"

private const val TITLE_MAX_LENGTH = 40

fun sessionTitleFrom(text: String): String {
    val collapsed = text.replace(Regex("\\s+"), " ").trim()
    return when {
        collapsed.isEmpty() -> UNTITLED_SESSION
        collapsed.length <= TITLE_MAX_LENGTH -> collapsed
        else -> collapsed.take(TITLE_MAX_LENGTH).trimEnd() + "…"
    }
}
