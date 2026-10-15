package com.example.scanner

object Gs1Parser {

    data class Gs1Data(
        val gtin: String? = null,
        val batch: String? = null,
        val expiry: String? = null,
        val serial: String? = null
    )

    fun parse(raw: String): Gs1Data {
        var text = raw.trim()
        var gtin: String? = null
        var batch: String? = null
        var expiry: String? = null
        var serial: String? = null

        // Parenthesized format (01)...(10)...
        if (text.contains("(")) {
            val gtinMatch = Regex("""\(01\)(\d{14}|\d{12,13})""").find(text)
            if (gtinMatch != null) gtin = gtinMatch.groupValues[1]

            val batchMatch = Regex("""\(10\)([A-Za-z0-9\-_]{1,20})""").find(text)
            if (batchMatch != null) batch = batchMatch.groupValues[1]

            val expMatch = Regex("""\(17\)(\d{6})""").find(text)
            if (expMatch != null) expiry = expMatch.groupValues[1]

            val serialMatch = Regex("""\(21\)([A-Za-z0-9\-_]{1,20})""").find(text)
            if (serialMatch != null) serial = serialMatch.groupValues[1]
        } else if (text.startsWith("01") && text.length >= 16) {
            // Raw AI stream
            try {
                gtin = text.substring(2, 16)
                var remaining = text.substring(16)
                if (remaining.startsWith("17") && remaining.length >= 8) {
                    expiry = remaining.substring(2, 8)
                    remaining = remaining.substring(8)
                }
                if (remaining.startsWith("10") && remaining.length > 2) {
                    batch = remaining.substring(2).take(20)
                }
            } catch (_: Exception) {}
        }

        return Gs1Data(gtin = gtin, batch = batch, expiry = expiry, serial = serial)
    }
}
