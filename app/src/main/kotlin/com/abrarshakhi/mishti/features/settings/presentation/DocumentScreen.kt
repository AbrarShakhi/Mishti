package com.abrarshakhi.mishti.features.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.abrarshakhi.mishti.features.chat.presentation.MarkdownReply
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@get:StringRes
val AppDocument.titleRes: Int
    get() = when (this) {
        AppDocument.About -> R.string.document_about
        AppDocument.Credits -> R.string.document_credits
        AppDocument.Privacy -> R.string.document_privacy
        AppDocument.Terms -> R.string.document_terms
        AppDocument.Contributing -> R.string.document_contributing
    }

@get:StringRes
val AppDocument.subtitleRes: Int
    get() = when (this) {
        AppDocument.About -> R.string.document_about_subtitle
        AppDocument.Credits -> R.string.document_credits_subtitle
        AppDocument.Privacy, AppDocument.Terms -> R.string.document_draft_subtitle
        AppDocument.Contributing -> R.string.document_contributing_subtitle
    }

@Composable
fun DocumentRoute(
    document: AppDocument,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markdown by produceState<String?>(initialValue = null, document) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(document.assetPath).bufferedReader().use { it.readText() }
            }.map { unindentWrappedLines(stripLeadingTitle(it)) }.getOrDefault("")
        }
    }

    DocumentScreen(
        document = document,
        markdown = markdown,
        onBack = onBack,
        onOpenDocument = onOpenDocument,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentScreen(
    document: AppDocument,
    markdown: String?,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val browser = LocalUriHandler.current
    val linkHandler = remember(browser, onOpenDocument) {
        object : UriHandler {
            override fun openUri(uri: String) {
                when (val link = resolveDocumentLink(uri)) {
                    is DocumentLink.Internal -> onOpenDocument(link.document)
                    is DocumentLink.External -> browser.openUri(link.url)
                }
            }
        }
    }

    SettingsScaffold(
        title = stringResource(document.titleRes),
        subtitle = stringResource(document.subtitleRes),
        onBack = onBack,
        modifier = modifier,
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier.padding(Spacing.ExtraLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        markdown == null -> LoadingIndicator(
                            modifier = Modifier
                                .padding(vertical = Spacing.ExtraLarge)
                                .size(48.dp),
                        )

                        markdown.isEmpty() -> Text(
                            text = stringResource(R.string.document_missing),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        else -> CompositionLocalProvider(LocalUriHandler provides linkHandler) {
                            MarkdownReply(markdown = markdown, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        item { Box(Modifier.height(Spacing.Large)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentScreenPreview() {
    MishtiTheme {
        DocumentScreen(
            document = AppDocument.About,
            markdown = "**Mishti** means *sweet* in Bengali.\n\n## What it does\n\n- Private chat\n- [Credits](credits.md)",
            onBack = {},
            onOpenDocument = {},
        )
    }
}
