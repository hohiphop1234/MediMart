package com.example.medimart.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Text(
        text = parseMarkdownToAnnotatedString(markdown),
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

fun parseMarkdownToAnnotatedString(markdown: String): AnnotatedString {
    // Remove 'null' or literal 'null' at the beginning
    var cleanedText = markdown.removePrefix("null").trimStart()
    if (cleanedText.startsWith("null")) {
        cleanedText = cleanedText.substring(4).trimStart()
    }

    // 1. Normalize escaped line breaks (\\n, \\r\\n) and Windows CRLF (\r\n) to \n
    cleanedText = cleanedText
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\r\n", "\n")
        .replace("\r", "\n")

    // 2. Insert newlines before section headers (###, ##, #) if preceded by text without a newline
    cleanedText = cleanedText.replace(Regex("([^\\n])\\s*(#{1,3}\\s+)")) { match ->
        "${match.groupValues[1]}\n\n${match.groupValues[2]}"
    }

    // 3. Insert newlines before bullet items (*, -, +) if preceded by text without a newline
    cleanedText = cleanedText.replace(Regex("([^\\n])\\s+([*\\-+]\\s+)")) { match ->
        "${match.groupValues[1]}\n${match.groupValues[2]}"
    }

    // 4. Insert newlines before numbered items (1.  2.  3.  etc.) if preceded by text without a newline (and not header #)
    cleanedText = cleanedText.replace(Regex("([^\\n#])\\s+(\\d+\\.\\s+)")) { match ->
        "${match.groupValues[1]}\n${match.groupValues[2]}"
    }

    return buildAnnotatedString {
        val lines = cleanedText.split("\n")
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()

            when {
                trimmedLine.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        appendInlineMarkdown(trimmedLine.removePrefix("### "))
                    }
                }
                trimmedLine.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        appendInlineMarkdown(trimmedLine.removePrefix("## "))
                    }
                }
                trimmedLine.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)) {
                        appendInlineMarkdown(trimmedLine.removePrefix("# "))
                    }
                }
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("+ ") -> {
                    append("•  ")
                    appendInlineMarkdown(trimmedLine.substring(2))
                }
                trimmedLine.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val numberPrefix = Regex("^\\d+\\.\\s+").find(trimmedLine)?.value ?: ""
                    append(numberPrefix)
                    appendInlineMarkdown(trimmedLine.removePrefix(numberPrefix))
                }
                else -> {
                    appendInlineMarkdown(line)
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    val tokenRegex = Regex("(\\*\\*|__)(.*?)\\1|([*_])(.*?)\\3|`([^`]+)`")
    var lastIndex = 0

    tokenRegex.findAll(text).forEach { matchResult ->
        if (matchResult.range.first > lastIndex) {
            append(text.substring(lastIndex, matchResult.range.first))
        }

        when {
            // Bold (**...** or __...__)
            matchResult.groupValues[1].isNotEmpty() -> {
                val boldContent = matchResult.groupValues[2]
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldContent)
                }
            }
            // Italic (*...* or _..._)
            matchResult.groupValues[3].isNotEmpty() -> {
                val italicContent = matchResult.groupValues[4]
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicContent)
                }
            }
            // Inline code (`...`)
            matchResult.groupValues[5].isNotEmpty() -> {
                val codeContent = matchResult.groupValues[5]
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(codeContent)
                }
            }
        }

        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
