package dev.hossain.weatheralert.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Parses basic markdown-like syntax to [AnnotatedString].
 */
fun parseMarkdown(markdown: String): AnnotatedString {
    // Match **bold**
    val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
    // Match *italic* or _italic_
    val italicRegex = "(\\*|_)(.*?)\\1".toRegex()
    // Match list items
    val listRegex = "^(?:\\*|-|\\d+\\.) (.*)".toRegex(RegexOption.MULTILINE)

    return buildAnnotatedString {
        var currentIndex = 0

        // Function to process matches
        fun processMatch(
            match: MatchResult,
            style: SpanStyle,
        ) {
            append(markdown.substring(currentIndex, match.range.first)) // Append text before match
            pushStyle(style)
            // Append styled match content
            append(match.groups[2]!!.value)
            pop()
            currentIndex = match.range.last + 1
        }

        // Handle list items
        listRegex.findAll(markdown).forEach { match ->
            // Add a bullet point
            append("• ${match.groups[1]!!.value}\n")
            currentIndex = match.range.last + 1
        }

        // Reset index after processing lists
        currentIndex = 0

        // Process bold and italic in order
        boldRegex.findAll(markdown).forEach { processMatch(it, SpanStyle(fontWeight = FontWeight.Bold)) }
        italicRegex.findAll(markdown).forEach { processMatch(it, SpanStyle(fontStyle = FontStyle.Italic)) }

        // Append remaining text
        append(markdown.substring(currentIndex))
    }
}
