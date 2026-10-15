package com.example.scanner

object BatchParser {

    private val batchKeywordRegex = Regex(
        """(?i)\b(?:BATCH\s*(?:NO|CODE)?|B\.?\s*NO|LOT\s*(?:NO)?|BN)\s*[:.\-]?\s*([A-Za-z0-9\-–]{3,18})"""
    )

    fun parse(text: String): String? {
        val match = batchKeywordRegex.find(text)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            return sanitizeBatch(candidate)
        }

        // Check standalone lines that resemble stamped batch syntax (e.g. 8 digits, or 2 digits + letter + 3 digits)
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.matches(Regex("""^[0-9]{7,10}$""")) ||
                trimmed.matches(Regex("""^[0-9]{2}[A-Za-z][0-9]{3,4}$""")) ||
                trimmed.matches(Regex("""^[A-Za-z]{2}-[A-Za-z0-9\-]{4,10}$"""))
            ) {
                return sanitizeBatch(trimmed)
            }
        }

        return null
    }

    private fun sanitizeBatch(raw: String): String {
        var result = raw.replace("–", "-").trim()
        // If it looks almost purely numeric, fix common OCR letter-for-digit confusions
        val digitCount = result.count { it.isDigit() }
        if (digitCount >= result.length - 1 && result.length >= 6) {
            result = result.replace('O', '0')
                .replace('o', '0')
                .replace('I', '1')
                .replace('l', '1')
                .replace('S', '5')
                .replace('s', '5')
        }
        return result
    }
}
