package dev.hossain.weatheralert.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Parses very basic markdown text to [AnnotatedString].
 */
fun parseMarkdown(markdown: String): AnnotatedString =
    buildAnnotatedString {
        val lines = markdown.lines()
        for (line in lines) {
            var currentIndex = 0
            while (currentIndex < line.length) {
                when {
                    line.startsWith("- ", currentIndex) -> {
                        // List item
                        append("• ")
                        currentIndex += 2
                    }
                    line.startsWith("* ", currentIndex) -> {
                        // List item
                        append("• ")
                        currentIndex += 2
                    }
                    line.startsWith("**", currentIndex) -> {
                        // Bold text
                        val endIndex = line.indexOf("**", currentIndex + 2)
                        if (endIndex != -1) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(line.substring(currentIndex + 2, endIndex))
                            }
                            currentIndex = endIndex + 2
                        } else {
                            append(line.substring(currentIndex))
                            currentIndex = line.length
                        }
                    }
                    line.startsWith("_", currentIndex) -> {
                        // Italic text
                        val endIndex = line.indexOf("_", currentIndex + 1)
                        if (endIndex != -1) {
                            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(line.substring(currentIndex + 1, endIndex))
                            }
                            currentIndex = endIndex + 1
                        } else {
                            append(line.substring(currentIndex))
                            currentIndex = line.length
                        }
                    }
                    else -> {
                        // Regular text or mixed styles
                        val nextSpecialChar =
                            listOf(line.indexOf("**", currentIndex), line.indexOf("_", currentIndex))
                                .filter { it != -1 }
                                .minOrNull() ?: line.length
                        append(line.substring(currentIndex, nextSpecialChar))
                        currentIndex = nextSpecialChar
                    }
                }
            }
            append("\n")
        }
    }

/**
 * Strips markdown syntax except list items.
 * This is useful to show notes in the notification without any markdown syntax
 * which is not supported by Android notification surface.
 *
 * @param textWithMarkdownSyntax The markdown text to be stripped.
 * @return A text with markdown syntax removed from the source [textWithMarkdownSyntax].
 */
internal fun stripMarkdownExceptLists(textWithMarkdownSyntax: String): String {
    val lines = textWithMarkdownSyntax.lines()
    val stringBuilder = StringBuilder()

    for (line in lines) {
        when {
            line.startsWith("- ") || line.startsWith("* ") -> {
                // List item, strip markdown but keep the list item
                val strippedLine =
                    line
                        .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // Remove bold text
                        .replace(Regex("_([^_]+)_"), "$1") // Remove italic text
                stringBuilder.append(strippedLine).append("\n")
            }
            else -> {
                // Remove other markdown syntax
                val strippedLine =
                    line
                        .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // Remove bold text
                        .replace(Regex("_([^_]+)_"), "$1") // Remove italic text
                stringBuilder.append(strippedLine).append("\n")
            }
        }
    }

    return stringBuilder.toString().trim()
}
