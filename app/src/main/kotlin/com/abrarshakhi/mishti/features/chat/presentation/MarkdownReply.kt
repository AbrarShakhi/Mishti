package com.abrarshakhi.mishti.features.chat.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.abrarshakhi.mishti.common.ui.theme.CodeFontFamily
import com.abrarshakhi.mishti.common.ui.theme.Spacing
import com.mikepenz.markdown.compose.LocalBulletListHandler
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownBulletList
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownHeader
import com.mikepenz.markdown.compose.elements.listDepth
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.RoundedCornerSpanPainter
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.BulletHandler
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.MarkdownAnimations
import com.mikepenz.markdown.model.MarkdownAnnotator
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownDimens
import com.mikepenz.markdown.model.MarkdownExtendedSpans
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import com.mikepenz.markdown.model.StreamingMarkdownState
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.parseMarkdown
import com.mikepenz.markdown.model.rememberStreamingMarkdownState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import kotlin.time.Duration.Companion.milliseconds

/** The shortest gap between two renders of a streaming reply, so a fast model can't flood the UI. */
private val StreamingRenderInterval = 50.milliseconds

/** Extra room above a heading, so it reads as the start of its section, not the end of the last. */
private val HeadingTopSpace = Spacing.Small

/** GitHub-flavoured Markdown, the dialect models write: tables, strikethrough, task lists, bare links. */
private val ReplyFlavour = GFMFlavourDescriptor()
private val ReplyParser = MarkdownParser(ReplyFlavour, cancellationToken = CancellationToken.NonCancellable)

/**
 * Images are left out, as replies are written offline with nothing to fetch them from. One shared
 * instance: the renderer compares it between recompositions, and a new one would re-render every
 * block.
 */
private val NoImages: ImageTransformer = NoOpImageTransformerImpl()

/** Code within a sentence, sized relative to it so that it scales with headings too. */
private val InlineCodeStyle = SpanStyle(fontFamily = CodeFontFamily, fontSize = 0.9.em)

private const val NoBreakSpace = '\u00A0'

/**
 * A finished reply, rendered as Markdown. It is parsed as it composes, so it never shows up
 * empty, not even for the moment the streaming reply hands over to it.
 */
@Composable
internal fun MarkdownReply(markdown: String, modifier: Modifier = Modifier) {
    val state = remember(markdown) {
        parseMarkdown(markdown, flavour = ReplyFlavour, parser = ReplyParser)
    }
    val style = replyStyle()
    Markdown(
        state = state,
        colors = style.colors,
        typography = style.typography,
        modifier = modifier,
        padding = style.padding,
        dimens = style.dimens,
        imageTransformer = NoImages,
        annotator = style.annotator,
        extendedSpans = style.extendedSpans,
        components = ReplyComponents,
        animations = style.animations,
        // Should the parser ever fail, the reply still shows, as written.
        error = { errorModifier ->
            Text(text = markdown, modifier = errorModifier, style = style.typography.paragraph)
        },
    )
}

/**
 * A reply that is still arriving, rendered as Markdown as it grows. Blocks that are complete keep
 * their parse; only the one still being written is parsed again, at most once per
 * [StreamingRenderInterval]. [placeholder] shows until there is a block to render.
 */
@Composable
internal fun StreamingMarkdownReply(
    markdown: String,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberStreamingMarkdownState(flavour = ReplyFlavour)
    val latestMarkdown by rememberUpdatedState(markdown)
    LaunchedEffect(state) {
        snapshotFlow { latestMarkdown }
            .conflate()
            .collect { text ->
                // A reply only ever grows (the next one is a new list item), so the parser is
                // handed just the part it hasn't seen.
                val parsedLength = state.content.length
                if (text.length > parsedLength) state.append(text.substring(parsedLength))
                delay(StreamingRenderInterval)
            }
    }

    // Waiting on the parse rather than on the text leaves no empty frame between the two.
    val snapshot by state.snapshot.collectAsState()
    if (!snapshot.hasBlocks()) {
        placeholder()
    } else {
        val style = replyStyle()
        Markdown(
            streamingMarkdownState = state,
            colors = style.colors,
            typography = style.typography,
            modifier = modifier,
            padding = style.padding,
            dimens = style.dimens,
            imageTransformer = NoImages,
            annotator = style.annotator,
            extendedSpans = style.extendedSpans,
            components = ReplyComponents,
            animations = style.animations,
        )
    }
}

private fun StreamingMarkdownState.Snapshot.hasBlocks(): Boolean =
    (stableAst.asSequence() + unstableAstTail).any { node ->
        node.type != MarkdownTokenTypes.EOL && node.type != MarkdownTokenTypes.WHITE_SPACE
    }

/** How replies look: Markdown's elements mapped onto the app's type scale and colours. */
private class ReplyStyle(
    val colors: MarkdownColors,
    val typography: MarkdownTypography,
    val padding: MarkdownPadding,
    val dimens: MarkdownDimens,
    val annotator: MarkdownAnnotator,
    val extendedSpans: MarkdownExtendedSpans,
    val animations: MarkdownAnimations,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun replyStyle(): ReplyStyle {
    val colorScheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val body = type.bodyLarge
    val chipColor = colorScheme.surfaceContainerHighest
    return ReplyStyle(
        colors = markdownColor(
            text = colorScheme.onSurface,
            dividerColor = colorScheme.outlineVariant,
            tableBackground = colorScheme.surfaceContainerLow,
        ),
        typography = markdownTypography(
            h1 = type.headlineSmallEmphasized,
            h2 = type.titleLargeEmphasized,
            h3 = type.titleMediumEmphasized,
            h4 = type.titleMediumEmphasized,
            h5 = type.titleMediumEmphasized,
            h6 = type.titleMediumEmphasized,
            text = body,
            paragraph = body,
            ordered = body,
            bullet = body,
            list = body,
            code = type.bodyMedium.copy(fontFamily = CodeFontFamily),
            quote = body.copy(color = colorScheme.onSurfaceVariant),
            textLink = TextLinkStyles(
                style = SpanStyle(color = colorScheme.primary, textDecoration = TextDecoration.Underline),
            ),
            table = type.bodyMedium,
        ),
        // The gap also opens the first block, which centres a first line of body text on the
        // avatar. Lists get no gap of their own, so they sit as far from a paragraph as another
        // paragraph would.
        padding = markdownPadding(block = Spacing.ExtraSmall, list = 0.dp),
        dimens = markdownDimens(blockQuoteThickness = 3.dp, tableCornerSize = 12.dp),
        annotator = remember(chipColor) { inlineCodeAnnotator(chipColor) },
        // Rounds the inline code chips' backgrounds.
        extendedSpans = markdownExtendedSpans {
            remember {
                ExtendedSpans(
                    RoundedCornerSpanPainter(
                        cornerRadius = 6.sp,
                        padding = RoundedCornerSpanPainter.TextPaddingValues(vertical = 1.sp),
                    ),
                )
            }
        },
        // A streaming reply gains lines all the time; animating each one would keep the list
        // laying itself out again.
        animations = markdownAnimations(animateTextSize = { this }),
    )
}

/**
 * Inline code as a chip that wraps as one piece. The renderer pads code with plain spaces, which
 * can wrap onto the line before and leave a sliver of chip behind; no-break spaces stay with the
 * code, and in the body's face they pad by a space rather than by a whole monospaced cell.
 */
private fun inlineCodeAnnotator(chipColor: Color): MarkdownAnnotator =
    markdownAnnotator { content, node ->
        if (node.type != MarkdownElementTypes.CODE_SPAN) return@markdownAnnotator false
        withStyle(SpanStyle(background = chipColor)) {
            append(NoBreakSpace)
            withStyle(InlineCodeStyle) { append(node.codeSpanText(content)) }
            append(NoBreakSpace)
        }
        true
    }

/**
 * The code between a code span's backticks. As in CommonMark, line breaks become spaces, and one
 * space just inside each pair of backticks is dropped: it's there so code can start or end with a
 * backtick.
 */
internal fun ASTNode.codeSpanText(content: CharSequence): String {
    val code = content
        .substring(children.first().endOffset, children.last().startOffset)
        .replace('\n', ' ')
    val padded = code.length >= 2 && code.first() == ' ' && code.last() == ' ' && code.isNotBlank()
    return if (padded) code.substring(1, code.length - 1) else code
}

private val ReplyComponents = markdownComponents(
    codeFence = { model ->
        MarkdownCodeFence(model.content, model.node, model.typography.code) { code, language, style ->
            CodeBlock(code = code, language = language, style = style)
        }
    },
    codeBlock = { model ->
        MarkdownCodeBlock(model.content, model.node, model.typography.code) { code, language, style ->
            CodeBlock(code = code, language = language, style = style)
        }
    },
    unorderedList = { model ->
        CompositionLocalProvider(LocalBulletListHandler provides DepthBullets) {
            MarkdownBulletList(model.content, model.node, model.typography.bullet, model.listDepth)
        }
    },
    heading1 = heading { h1 },
    heading2 = heading { h2 },
    heading3 = heading { h3 },
    heading4 = heading { h4 },
    heading5 = heading { h5 },
    heading6 = heading { h6 },
    setextHeading1 = heading(MarkdownTokenTypes.SETEXT_CONTENT) { h1 },
    setextHeading2 = heading(MarkdownTokenTypes.SETEXT_CONTENT) { h2 },
    checkbox = { model -> MarkdownCheckBox(model.content, model.node, model.typography.text) },
)

/** Bullets that change with depth, as in documents: a disc, then a circle, then a square. */
private val DepthBullets = BulletHandler { _, _, _, _, depth ->
    when (depth % 3) {
        0 -> "• "
        1 -> "◦ "
        else -> "▪ "
    }
}

private fun heading(
    contentType: IElementType = MarkdownTokenTypes.ATX_CONTENT,
    style: MarkdownTypography.() -> TextStyle,
): MarkdownComponent = { model ->
    val topSpace = if (model.node.startOffset > 0) HeadingTopSpace else 0.dp
    Box(modifier = Modifier.padding(top = topSpace)) {
        MarkdownHeader(model.content, model.node, model.typography.style(), contentType)
    }
}

/**
 * Code in JetBrains Mono under its language and a copy button. Long lines scroll sideways rather
 * than wrap, so the code keeps its shape.
 */
@Composable
private fun CodeBlock(code: String, language: String?, style: TextStyle) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.ExtraSmall),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column {
            // The label and button aren't code, so a selection passes over them.
            DisableSelection {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.Large, end = Spacing.ExtraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = language.orEmpty(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CopyButton(text = code, contentDescription = "Copy code")
                }
            }
            Text(
                text = code,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = Spacing.Large, end = Spacing.Large, bottom = Spacing.Large),
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                style = style,
            )
        }
    }
}
