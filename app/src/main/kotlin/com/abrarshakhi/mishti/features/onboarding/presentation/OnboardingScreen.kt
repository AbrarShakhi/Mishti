package com.abrarshakhi.mishti.features.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.ui.theme.MishtiTheme
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class OnboardingPage(
    @RawRes val animation: Int,
    val title: String,
    val body: String,
)

private val Pages = listOf(
    OnboardingPage(
        animation = R.raw.onboarding_chat,
        title = "Meet Mishti",
        body = "A language model that runs entirely on your phone.",
    ),
    OnboardingPage(
        animation = R.raw.onboarding_private,
        title = "Private and offline",
        body = "Conversations never leave this device, and once a model is downloaded no " +
            "network is needed.",
    ),
    OnboardingPage(
        animation = R.raw.onboarding_personalize,
        title = "Make it yours",
        body = "Choose the model, the colours and the typeface, and tune how it answers.",
    ),
)

/** How far the art trails the page as it is swiped, as a fraction of the page width. */
private const val ArtParallax = 0.3f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { Pages.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == Pages.lastIndex

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = Spacing.Small),
                contentAlignment = Alignment.CenterEnd,
            ) {
                SkipButton(visible = !isLastPage, onClick = onContinue)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                PageContent(
                    page = Pages[page],
                    isActive = pagerState.settledPage == page,
                    offset = pagerState.offsetOf(page),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ExtraLarge, vertical = Spacing.ExtraLarge),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PageIndicator(
                    pageCount = Pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        if (isLastPage) {
                            onContinue()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                    modifier = Modifier
                        .heightIn(min = ButtonDefaults.MediumContainerHeight)
                        .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "OnboardingButton",
                    ) { last ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (last) "Get started" else "Next",
                                style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
                            )
                            if (!last) {
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Skip, which steps aside on the last page where "Get started" says the same thing. */
@Composable
private fun SkipButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        TextButton(onClick = onClick) { Text("Skip") }
    }
}

/** How many pages [page] is from the settled position: 0 when centred, ±1 one page away. */
private fun PagerState.offsetOf(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageContent(
    page: OnboardingPage,
    isActive: Boolean,
    offset: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.ExtraExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingAnimation(
            animation = page.animation,
            isActive = isActive,
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 360.dp)
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .graphicsLayer { translationX = offset * size.width * ArtParallax },
        )
        Spacer(Modifier.height(Spacing.ExtraLarge))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = 1f - offset.absoluteValue },
        )
        Spacer(Modifier.height(Spacing.Medium))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .graphicsLayer { alpha = 1f - offset.absoluteValue },
        )
    }
}

/** Dots for the pages, the current one stretched into a pill. */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Page ${currentPage + 1} of $pageCount"
        },
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "IndicatorWidth",
            )
            val color by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "IndicatorColor",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MishtiTheme { OnboardingScreen(onContinue = {}) }
}
