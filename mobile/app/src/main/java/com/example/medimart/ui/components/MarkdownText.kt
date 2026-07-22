package com.example.medimart.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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

    return buildAnnotatedString {
        val lines = cleanedText.split("\n")
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trimEnd()

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
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    append("•  ")
                    appendInlineMarkdown(trimmedLine.substring(2))
                }
                else -> {
                    appendInlineMarkdown(trimmedLine)
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    val boldRegex = Regex("(\\*\\*|__)(.*?)\\1")
    var lastIndex = 0

    boldRegex.findAll(text).forEach { matchResult ->
        if (matchResult.range.first > lastIndex) {
            append(text.substring(lastIndex, matchResult.range.first))
        }

        val boldContent = matchResult.groupValues[2]
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(boldContent)
        }

        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
