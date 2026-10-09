package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.ui.theme.Spacing

@Composable
internal fun ChatTopBar(
    title: String,
    engineState: EngineState,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = sessionDisplayTitle(title.ifEmpty { stringResource(R.string.app_name) }),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = { ModelStatus(engineState) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.chat_open_conversations)
                )
            }
        },
        actions = {
            IconButton(onClick = onNewChat) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.drawer_new_chat)
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ModelStatus(engineState: EngineState) {
    val colors = MaterialTheme.colorScheme
    val (label, dotColor) = when (engineState) {
        is EngineState.Ready -> engineState.model.name to colors.primary
        is EngineState.Loading ->
            stringResource(R.string.chat_status_loading, engineState.model.name) to colors.tertiary

        is EngineState.Failed -> stringResource(R.string.chat_status_failed) to colors.error
        EngineState.Idle -> stringResource(R.string.chat_status_idle) to colors.outline
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusDot(dotColor)
        Spacer(Modifier.width(Spacing.Small))
        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusDot(color: Color) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "StatusDot",
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(animatedColor, CircleShape),
    )
}
