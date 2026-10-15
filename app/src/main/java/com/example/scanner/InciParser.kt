package com.example.scanner

object InciParser {

    private val inciHeaderRegex = Regex(
        """(?i)\b(?:INGREDIENTS|COMPOSITION|INGRÉDIENTS)\s*[:.\-]?\s*""",
        RegexOption.MULTILINE
    )

    fun parse(text: String): List<String> {
        val match = inciHeaderRegex.find(text)
        val targetText = if (match != null) {
            text.substring(match.range.last + 1)
        } else {
            text
        }

        // Clean up text and split on commas, semicolons or bullet points
        val cleaned = targetText
            .replace("\n", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val tokens = cleaned.split(Regex("[,;•]"))
            .map { it.trim().trimEnd('.') }
            .filter { it.length in 2..60 }

        return tokens
    }
}
