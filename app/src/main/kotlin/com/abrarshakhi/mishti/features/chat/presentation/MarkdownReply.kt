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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.abrarshakhi.mishti.R
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

private val StreamingRenderInterval = 50.milliseconds

private val HeadingTopSpace = Spacing.Small

private val ReplyFlavour = GFMFlavourDescriptor()
private val ReplyParser =
    MarkdownParser(ReplyFlavour, cancellationToken = CancellationToken.NonCancellable)

private val NoImages: ImageTransformer = NoOpImageTransformerImpl()

private val InlineCodeStyle = SpanStyle(fontFamily = CodeFontFamily, fontSize = 0.9.em)

private const val NoBreakSpace = '\u00A0'

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
        error = { errorModifier ->
            Text(text = markdown, modifier = errorModifier, style = style.typography.paragraph)
        },
    )
}

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
                val parsedLength = state.content.length
                if (text.length > parsedLength) state.append(text.substring(parsedLength))
                delay(StreamingRenderInterval)
            }
    }

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
                style = SpanStyle(
                    color = colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
            ),
            table = type.bodyMedium,
        ),
        padding = markdownPadding(block = Spacing.ExtraSmall, list = 0.dp),
        dimens = markdownDimens(blockQuoteThickness = 3.dp, tableCornerSize = 12.dp),
        annotator = remember(chipColor) { inlineCodeAnnotator(chipColor) },
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
        animations = markdownAnimations(animateTextSize = { this }),
    )
}

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

internal fun ASTNode.codeSpanText(content: CharSequence): String {
    val code = content
        .substring(children.first().endOffset, children.last().startOffset)
        .replace('\n', ' ')
    val padded = code.length >= 2 && code.first() == ' ' && code.last() == ' ' && code.isNotBlank()
    return if (padded) code.substring(1, code.length - 1) else code
}

private val ReplyComponents = markdownComponents(
    codeFence = { model ->
        MarkdownCodeFence(
            model.content,
            model.node,
            model.typography.code
        ) { code, language, style ->
            CodeBlock(code = code, language = language, style = style)
        }
    },
    codeBlock = { model ->
        MarkdownCodeBlock(
            model.content,
            model.node,
            model.typography.code
        ) { code, language, style ->
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
                    CopyButton(
                        text = code,
                        contentDescription = stringResource(R.string.chat_copy_code)
                    )
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
