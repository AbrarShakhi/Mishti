package com.abrarshakhi.mishti.models.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.resources.action_try_again
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.feature.model.resources.models_cancel_download
import com.abrarshakhi.mishti.feature.model.resources.models_checking_file
import com.abrarshakhi.mishti.feature.model.resources.models_download_named
import com.abrarshakhi.mishti.feature.model.resources.models_download_size
import com.abrarshakhi.mishti.feature.model.resources.models_on_shelf
import com.abrarshakhi.mishti.feature.model.resources.models_progress
import com.abrarshakhi.mishti.feature.model.resources.models_retry_named
import com.abrarshakhi.mishti.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.models.domain.model.TransferStatus
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignsystemRes
import com.abrarshakhi.mishti.feature.model.resources.Res as FeatureModelRes

@Composable
internal fun formatSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "${decimalTenths(bytes / 1_000_000_000.0)} GB"
    bytes >= 1_000_000 -> "${decimalTenths(bytes / 1_000_000.0)} MB"
    bytes >= 1_000 -> "${decimalTenths(bytes / 1_000.0)} KB"
    else -> "$bytes B"
}

fun gigabytes(bytes: Long): String = decimalTenths(bytes / 1_000_000_000.0)

internal fun decimalTenths(value: Double): String {
    val tenths = kotlin.math.round(value * 10).toLong()
    return "${tenths / 10}.${tenths % 10}"
}
