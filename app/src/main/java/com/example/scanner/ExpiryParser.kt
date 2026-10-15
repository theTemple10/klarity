package com.example.scanner

object ExpiryParser {

    private val expRegex = Regex(
        """(?i)\b(?:EXP(?:\.?|\s*DATE)?|BEST\s*BEFORE|BB|USE\s*BY)\s*[:.\-]?\s*([0-9]{1,2}[/\.\-][0-9]{2,4}|[A-Za-z]{3}[/\.\-\s][0-9]{2,4})"""
    )

    fun parse(text: String): String? {
        val match = expRegex.find(text)
        return match?.groupValues?.get(1)?.trim()
    }
}
