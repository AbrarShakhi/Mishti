package com.abrarshakhi.mishti.features.chat.presentation

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val CopiedFeedbackMillis = 1_500L

/**
 * Copies [text] to the clipboard, then shows a check for a moment, so the copy is acknowledged
 * even on Android versions without the system's own clipboard confirmation.
 */
@Composable
internal fun CopyButton(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(CopiedFeedbackMillis.milliseconds)
            copied = false
        }
    }

    IconButton(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Mishti", text)))
                copied = true
            }
        },
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "CopyIcon",
        ) { isCopied ->
            Icon(
                imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = if (isCopied) "Copied" else contentDescription,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
