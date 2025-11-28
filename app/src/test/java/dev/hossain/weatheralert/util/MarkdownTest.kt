package dev.hossain.weatheralert.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MarkdownTest {
    @Test
    fun testParseMarkdown_boldText() {
        val markdown = "This is **bold** text."
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("This is ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("bold")
                    }
                    append(" text.")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_italicText() {
        val markdown = "This is _italic_ text."
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("This is ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("italic")
                    }
                    append(" text.")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_listItems() {
        val markdown = "- List item 1\n* List item 2\nNormal text"
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("• List item 1\n")
                    append("• List item 2\n")
                    append("Normal text")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_mixedContent() {
        val markdown =
            """
            Here is a list:
            - **Bold item**
            _Italic item_ followed by some text.
            """.trimIndent()
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("Here is a list:\n")
                    append("• ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Bold item")
                    }
                    append("\n")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Italic item")
                    }
                    append(" followed by some text.")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownExceptLists_boldText() {
        val markdown = "This is **bold** text."
        val expected = "This is bold text."
        assertThat(stripMarkdownSyntax(markdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownExceptLists_italicText() {
        val markdown = "This is _italic_ text."
        val expected = "This is italic text."
        assertThat(stripMarkdownSyntax(markdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownExceptLists_listItems() {
        val markdown = "- List item 1\n* List item 2\nNormal text"
        val expected = "- List item 1\n* List item 2\nNormal text"
        assertThat(stripMarkdownSyntax(markdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownExceptLists_mixedContent() {
        val multilineMarkdown =
            """
            Here is a list:
            - **Bold item**
            _Italic item_ followed by some text.
            """.trimIndent()
        val expected =
            """
            Here is a list:
            - Bold item
            Italic item followed by some text.
            """.trimIndent()
        assertThat(stripMarkdownSyntax(multilineMarkdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownExceptLists_moreMixedContent() {
        val multilineMarkdown =
            """
            Here is a list this that **must be done**:
            * Charge **batteries**
            * Check tire pressure
            * Order _groceries_
            
            Finally, you _should_ **help** your neighbors if needed.
            """.trimIndent()
        val expected =
            """
            Here is a list this that must be done:
            * Charge batteries
            * Check tire pressure
            * Order groceries
            
            Finally, you should help your neighbors if needed.
            """.trimIndent()
        assertThat(stripMarkdownSyntax(multilineMarkdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_emptyString() {
        val markdown = ""
        val expected = AnnotatedString.Builder().toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_plainTextOnly() {
        val markdown = "This is just plain text."
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("This is just plain text.")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testStripMarkdownSyntax_emptyString() {
        val markdown = ""
        assertThat(stripMarkdownSyntax(markdown)).isEmpty()
    }

    @Test
    fun testStripMarkdownSyntax_plainTextOnly() {
        val markdown = "This is just plain text."
        assertThat(stripMarkdownSyntax(markdown)).isEqualTo("This is just plain text.")
    }

    @Test
    fun testParseMarkdown_unclosedBoldMarker() {
        // When bold marker is not closed, it should append the rest of the line as-is
        val markdown = "This is **unclosed bold"
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("This is **unclosed bold")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }

    @Test
    fun testParseMarkdown_unclosedItalicMarker() {
        // When italic marker is not closed, it should append the rest of the line as-is
        val markdown = "This is _unclosed italic"
        val expected =
            AnnotatedString
                .Builder()
                .apply {
                    append("This is _unclosed italic")
                }.toAnnotatedString()
        assertThat(parseMarkdown(markdown)).isEqualTo(expected)
    }
}
