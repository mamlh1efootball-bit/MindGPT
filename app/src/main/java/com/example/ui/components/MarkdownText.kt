package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.VazirFontFamily

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimaryWhite,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 24.sp
) {
    val blocks = parseMarkdownBlocks(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val headingFontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        color = TextPrimaryWhite,
                        fontSize = headingFontSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (headingFontSize.value + 6).sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp, end = 10.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentActionBlue)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = color,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}. ",
                            color = AccentActionBlue,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = color,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(AccentActionBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = TextSecondaryGray,
                            fontSize = fontSize,
                            fontStyle = FontStyle.Italic,
                            lineHeight = lineHeight
                        )
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        color = color,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF16171B))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF202127))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language.isNotBlank()) language else "code",
                color = TextSecondaryGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                        Toast.makeText(context, "کد کپی شد", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = TextSecondaryGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "کپی",
                    color = TextSecondaryGray,
                    fontSize = 11.sp
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 19.sp
            )
        }
    }
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        if (trimmed.isEmpty()) {
            index++
            continue
        }

        // 1. Code block ```
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            index++
            while (index < lines.size && !lines[index].trim().startsWith("```")) {
                codeLines.add(lines[index])
                index++
            }
            if (index < lines.size && lines[index].trim().startsWith("```")) {
                index++
            }
            blocks.add(MarkdownBlock.CodeBlock(language = language, code = codeLines.joinToString("\n")))
            continue
        }

        // 2. Headings #
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val headingText = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 4), headingText))
            index++
            continue
        }

        // 3. Bullet items (*, -, •)
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
            val content = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(content))
            index++
            continue
        }

        // 4. Numbered items (1. 2.)
        val numberedRegex = Regex("^(\\d+)[.\\-]\\s*(.+)$")
        val numberMatch = numberedRegex.find(trimmed)
        if (numberMatch != null) {
            val num = numberMatch.groupValues[1]
            val content = numberMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(number = num, text = content))
            index++
            continue
        }

        // 5. Blockquotes >
        if (trimmed.startsWith(">")) {
            val content = trimmed.removePrefix(">").trim()
            blocks.add(MarkdownBlock.Blockquote(content))
            index++
            continue
        }

        // 6. Regular Paragraph
        val paraLines = mutableListOf<String>()
        while (index < lines.size) {
            val curLine = lines[index]
            val curTrim = curLine.trim()
            if (curTrim.isEmpty() ||
                curTrim.startsWith("```") ||
                curTrim.startsWith("#") ||
                curTrim.startsWith("* ") ||
                curTrim.startsWith("- ") ||
                curTrim.startsWith("• ") ||
                numberedRegex.matches(curTrim) ||
                curTrim.startsWith(">")
            ) {
                break
            }
            paraLines.add(curLine)
            index++
        }
        blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString("\n")))
    }

    return blocks
}

/**
 * Parses inline formatting: **bold**, *italic*, `inline code`
 */
fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val len = text.length

        while (cursor < len) {
            // Check for bold **text**
            if (cursor + 1 < len && text[cursor] == '*' && text[cursor + 1] == '*') {
                val end = text.indexOf("**", cursor + 2)
                if (end != -1) {
                    val boldContent = text.substring(cursor + 2, end)
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryWhite
                        )
                    )
                    append(boldContent)
                    pop()
                    cursor = end + 2
                    continue
                }
            }

            // Check for inline code `code`
            if (text[cursor] == '`') {
                val end = text.indexOf('`', cursor + 1)
                if (end != -1) {
                    val codeContent = text.substring(cursor + 1, end)
                    pushStyle(
                        SpanStyle(
                            background = Color.White.copy(alpha = 0.08f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF93C5FD)
                        )
                    )
                    append(" $codeContent ")
                    pop()
                    cursor = end + 1
                    continue
                }
            }

            // Check for italic *text* or _text_
            if (text[cursor] == '*' || text[cursor] == '_') {
                val delimiter = text[cursor]
                val end = text.indexOf(delimiter, cursor + 1)
                if (end != -1 && end > cursor + 1) {
                    val italicContent = text.substring(cursor + 1, end)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italicContent)
                    pop()
                    cursor = end + 1
                    continue
                }
            }

            append(text[cursor])
            cursor++
        }
    }
}
