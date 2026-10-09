package com.abrarshakhi.mishti.common.llm

const val THINK_OPEN = "<think>"
const val THINK_CLOSE = "</think>"

fun templateSupportsThinking(chatTemplate: String): Boolean = THINK_OPEN in chatTemplate

data class ReasoningSplit(
    val reasoning: String?,
    val answer: String,
    val isReasoning: Boolean,
)

fun splitReasoning(
    raw: String,
    complete: Boolean = false,
): ReasoningSplit {
    val leading = raw.trimStart()
    if (leading.startsWith(THINK_OPEN)) {
        val bodyStart = raw.indexOf(THINK_OPEN) + THINK_OPEN.length
        val end = raw.indexOf(THINK_CLOSE, bodyStart)
        if (end < 0) {
            val body = raw.substring(bodyStart)
            return ReasoningSplit(
                reasoning = (if (complete) body else body.withoutPartialTag(THINK_CLOSE)).trim(),
                answer = "",
                isReasoning = !complete,
            )
        }
        return ReasoningSplit(
            reasoning = raw.substring(bodyStart, end).trim().ifEmpty { null },
            answer = raw.substring(end + THINK_CLOSE.length).trimStart(),
            isReasoning = false,
        )
    }

    val orphanClose = raw.indexOf(THINK_CLOSE)
    if (orphanClose >= 0) {
        return ReasoningSplit(
            reasoning = raw.substring(0, orphanClose).trim().ifEmpty { null },
            answer = raw.substring(orphanClose + THINK_CLOSE.length).trimStart(),
            isReasoning = false,
        )
    }
    if (complete) return ReasoningSplit(reasoning = null, answer = raw, isReasoning = false)
    if (leading.isNotEmpty() && THINK_OPEN.startsWith(leading)) {
        return ReasoningSplit(reasoning = null, answer = "", isReasoning = false)
    }
    return ReasoningSplit(
        reasoning = null,
        answer = raw.withoutPartialTag(THINK_CLOSE),
        isReasoning = false
    )
}

private fun String.withoutPartialTag(tag: String): String {
    for (length in minOf(tag.length - 1, this.length) downTo 1) {
        if (endsWith(tag.substring(0, length))) return dropLast(length)
    }
    return this
}
