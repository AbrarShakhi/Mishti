package com.abrarshakhi.mishti.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.resources.action_try_again
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.core.ui.components.MarkdownReply
import com.abrarshakhi.mishti.feature.settings.resources.document_missing
import com.abrarshakhi.mishti.settings.domain.model.DocumentLink
import com.abrarshakhi.mishti.settings.domain.model.resolveDocumentLink
import com.abrarshakhi.mishti.settings.ui.components.SettingsScaffold
import com.abrarshakhi.mishti.settings.ui.utils.subtitleRes
import com.abrarshakhi.mishti.settings.ui.utils.titleRes
import com.abrarshakhi.mishti.settings.ui.viewmodels.contracts.DocumentUiState
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentScreen(
    document: AppDocument,
    state: DocumentUiState,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    onOpenExternal: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    val linkHandler = remember(onOpenExternal, onOpenDocument, document) {
        object : UriHandler {
            override fun openUri(uri: String) {
                when (val link = resolveDocumentLink(uri, document)) {
                    is DocumentLink.Internal -> onOpenDocument(link.document)
                    is DocumentLink.External -> onOpenExternal(link.url)
                }
            }
        }
    }

    SettingsScaffold(
        title = stringResource(document.titleRes),
        subtitle = stringResource(document.subtitleRes),
        onNavigate = onBack,
        isRootSetting = false,
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
                        state.isLoading -> LoadingIndicator(
                            modifier = Modifier
                                .padding(vertical = Spacing.ExtraLarge)
                                .size(48.dp),
                        )

                        state.hasFailed -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(FeatureSettingsRes.string.document_missing),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            TextButton(onClick = onRetry) {
                                Text(stringResource(CoreDesignsystemRes.string.action_try_again))
                            }
                        }

                        else -> CompositionLocalProvider(LocalUriHandler provides linkHandler) {
                            MarkdownReply(
                                markdown = state.markdown,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
        item { Box(Modifier.height(Spacing.Large)) }
    }
}

@Preview
@Composable
private fun DocumentScreenPreview() {
    MishtiTheme {
        DocumentScreen(
            document = AppDocument.About,
            state = DocumentUiState(
                isLoading = false,
                markdown = """
                **Mishti** means *sweet* in Bengali.
                
                ## What it does
                
                - Private chat
                - [Credits](credits.md)
                """.trimMargin(),
            ),
            onBack = {},
            onOpenDocument = {},
            onOpenExternal = {},
        )
    }
}
