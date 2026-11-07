package com.abrarshakhi.mishti.core.ui.markdown

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.ui.resources.action_copied
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import com.abrarshakhi.mishti.core.ui.resources.Res as CoreUiRes

private const val CopiedFeedbackMillis = 1_500L

@Composable
fun CopyButton(text: String, contentDescription: String, modifier: Modifier = Modifier) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(CopiedFeedbackMillis.milliseconds)
            copied = false
        }
    }

    IconButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(text))
            copied = true
        },
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "CopyIcon",
        ) { isCopied ->
            Icon(
                imageVector = if (isCopied) {
                    MaterialTheme.icons.actions.check.outline
                } else {
                    MaterialTheme.icons.actions.copy.outline
                },
                contentDescription = if (isCopied) {
                    stringResource(CoreUiRes.string.action_copied)
                } else {
                    contentDescription
                },
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
