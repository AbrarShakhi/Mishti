package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.CookieShape
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import kotlin.random.Random

private val HeroSize = 88.dp

private const val SuggestionCount = 4

fun pickSuggestions(pool: List<String>, count: Int, seed: Int): List<String> =
    pool.distinct().shuffled(Random(seed)).take(count)

@Composable
internal fun ChatEmptyState(
    engineState: EngineState,
    onSuggestion: (String) -> Unit,
    onOpenModels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enterSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exitSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight)
                .padding(horizontal = Spacing.ExtraLarge, vertical = Spacing.ExtraLarge),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = EmptyKind.of(engineState),
                transitionSpec = { fadeIn(enterSpec) togetherWith fadeOut(exitSpec) },
                label = "ChatEmptyState",
            ) { kind ->
                when (kind) {
                    EmptyKind.Ready -> ReadyState(engineState, onSuggestion)
                    EmptyKind.Loading -> LoadingState(engineState)
                    EmptyKind.NoModel -> NoModelState(onOpenModels)
                    EmptyKind.Failed -> FailedState(onOpenModels)
                }
            }
        }
    }
}

private enum class EmptyKind {
    Ready,
    Loading,
    NoModel,
    Failed,
    ;

    companion object {
        fun of(state: EngineState) = when (state) {
            is EngineState.Ready -> Ready
            is EngineState.Loading -> Loading
            is EngineState.Failed -> Failed
            EngineState.Idle -> NoModel
        }
    }
}

@Composable
private fun ReadyState(engineState: EngineState, onSuggestion: (String) -> Unit) {
    val modelName = (engineState as? EngineState.Ready)?.model?.name
    val pool = stringArrayResource(R.array.chat_suggestions).toList()
    var seed by rememberSaveable { mutableIntStateOf(Random.nextInt()) }
    val suggestions = remember(pool, seed) { pickSuggestions(pool, SuggestionCount, seed) }
    val enter = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exit = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    EmptyStateColumn(
        hero = { AppMark(size = HeroSize) },
        title = stringResource(R.string.chat_ready_title),
        body = if (modelName != null) {
            stringResource(R.string.chat_ready_body_model, modelName)
        } else {
            stringResource(R.string.chat_ready_body)
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = suggestions,
                transitionSpec = { fadeIn(enter) togetherWith fadeOut(exit) },
                label = "Suggestions",
            ) { shown ->
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        Spacing.Small,
                        Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small),
                ) {
                    shown.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { onSuggestion(suggestion) },
                            label = { Text(suggestion) },
                        )
                    }
                }
            }
            TextButton(
                onClick = { seed = Random.nextInt() },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.padding(top = Spacing.Small),
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.chat_suggestions_shuffle))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(engineState: EngineState) {
    val modelName = (engineState as? EngineState.Loading)?.model?.name
        ?: stringResource(R.string.chat_loading_fallback_name)

    EmptyStateColumn(
        hero = { LoadingIndicator(modifier = Modifier.size(HeroSize)) },
        title = stringResource(R.string.chat_loading_title, modelName),
        body = stringResource(R.string.chat_loading_body),
    )
}

@Composable
private fun NoModelState(onOpenModels: () -> Unit) {
    EmptyStateColumn(
        hero = { AppMark(size = HeroSize) },
        title = stringResource(R.string.chat_no_model_title),
        body = stringResource(R.string.chat_no_model_body),
    ) {
        Button(
            onClick = onOpenModels,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.chat_no_model_action))
        }
    }
}

@Composable
private fun FailedState(onOpenModels: () -> Unit) {
    EmptyStateColumn(
        hero = {
            Surface(
                modifier = Modifier.size(HeroSize),
                shape = CookieShape,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        },
        title = stringResource(R.string.chat_failed_title),
        body = stringResource(R.string.chat_failed_body),
    ) {
        Button(onClick = onOpenModels) { Text(stringResource(R.string.chat_failed_action)) }
    }
}

@Composable
private fun EmptyStateColumn(
    hero: @Composable () -> Unit,
    title: String,
    body: String,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        hero()
        Spacer(Modifier.height(Spacing.ExtraLarge))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.Small))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(Spacing.ExtraLarge))
            action()
        }
    }
}
