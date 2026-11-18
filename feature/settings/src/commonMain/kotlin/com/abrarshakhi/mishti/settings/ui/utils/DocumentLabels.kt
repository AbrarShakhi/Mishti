package com.abrarshakhi.mishti.settings.ui.utils

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
import com.abrarshakhi.mishti.feature.settings.resources.document_about
import com.abrarshakhi.mishti.feature.settings.resources.document_about_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.document_contributing
import com.abrarshakhi.mishti.feature.settings.resources.document_contributing_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.document_credits
import com.abrarshakhi.mishti.feature.settings.resources.document_credits_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.document_draft_subtitle
import com.abrarshakhi.mishti.feature.settings.resources.document_missing
import com.abrarshakhi.mishti.feature.settings.resources.document_privacy
import com.abrarshakhi.mishti.feature.settings.resources.document_terms
import com.abrarshakhi.mishti.settings.domain.model.DocumentLink
import com.abrarshakhi.mishti.settings.domain.model.resolveDocumentLink
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes
import com.abrarshakhi.mishti.feature.settings.resources.Res as FeatureSettingsRes

val AppDocument.titleRes: StringResource
    get() = when (this) {
        AppDocument.About -> FeatureSettingsRes.string.document_about
        AppDocument.Credits -> FeatureSettingsRes.string.document_credits
        AppDocument.Privacy -> FeatureSettingsRes.string.document_privacy
        AppDocument.Terms -> FeatureSettingsRes.string.document_terms
        AppDocument.Contributing -> FeatureSettingsRes.string.document_contributing
    }

val AppDocument.subtitleRes: StringResource
    get() = when (this) {
        AppDocument.About -> FeatureSettingsRes.string.document_about_subtitle
        AppDocument.Credits -> FeatureSettingsRes.string.document_credits_subtitle
        AppDocument.Privacy, AppDocument.Terms -> FeatureSettingsRes.string.document_draft_subtitle
        AppDocument.Contributing -> FeatureSettingsRes.string.document_contributing_subtitle
    }
