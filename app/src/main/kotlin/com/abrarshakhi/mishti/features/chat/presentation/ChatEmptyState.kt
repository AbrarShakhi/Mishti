package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.ui.components.AppMark
import com.abrarshakhi.mishti.common.ui.components.CookieShape
import com.abrarshakhi.mishti.common.ui.theme.Spacing

private val HeroSize = 88.dp

private val Suggestions = listOf(
    "Explain how rainbows form",
    "Write a haiku about the sea",
    "Suggest a name for a kitten",
    "Give me three quick dinner ideas",
)

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
    Ready, Loading, NoModel, Failed;

    companion object {
        fun of(state: EngineState) = when (state) {
            is EngineState.Ready -> Ready
            is EngineState.Loading -> Loading
            is EngineState.Failed -> Failed
            EngineState.Idle -> NoModel
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadyState(engineState: EngineState, onSuggestion: (String) -> Unit) {
    val modelName = (engineState as? EngineState.Ready)?.model?.name

    EmptyStateColumn(
        hero = { AppMark(size = HeroSize) },
        title = "How can I help?",
        body = if (modelName != null) {
            "$modelName runs on this phone, so your messages never leave it."
        } else {
            "Your messages never leave this phone."
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            Suggestions.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestion(suggestion) },
                    label = { Text(suggestion) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(engineState: EngineState) {
    val modelName = (engineState as? EngineState.Loading)?.model?.name ?: "the model"

    EmptyStateColumn(
        hero = { LoadingIndicator(modifier = Modifier.size(HeroSize)) },
        title = "Getting $modelName ready",
        body = "Loading it into memory takes a few seconds.",
    )
}

@Composable
private fun NoModelState(onOpenModels: () -> Unit) {
    EmptyStateColumn(
        hero = { AppMark(size = HeroSize) },
        title = "Choose a model to start",
        body = "Mishti runs a small language model entirely on this phone. Pick one from Mistir Bhandar to begin.",
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
            Text("Visit Mistir Bhandar")
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
                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(36.dp))
                }
            }
        },
        title = "The model couldn't be loaded",
        body = "It may need more memory than is free right now. Try a smaller model.",
    ) {
        Button(onClick = onOpenModels) { Text("Open models") }
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
