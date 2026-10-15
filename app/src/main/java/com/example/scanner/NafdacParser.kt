package com.example.scanner

object NafdacParser {

    private val nafdacRegex = Regex("""\b([A-Za-z]?\d{1,2})\s*[-–]\s*(\d{3,5}[A-Za-z]?)\b""")
    private val standaloneReg = Regex("""(?i)\b(?:NAFDAC|REG(?:\.|\s*NO)?)\s*[:.]?\s*([A-Za-z0-9\-–\s]{4,12})""")

    fun parse(text: String): String? {
        val lines = text.lines()

        // 1. First look on lines containing NAFDAC keyword
        for (line in lines) {
            if (line.contains("NAFDAC", ignoreCase = true)) {
                val match = nafdacRegex.find(line)
                if (match != null) {
                    val part1 = match.groupValues[1].uppercase()
                    val part2 = match.groupValues[2].uppercase()
                    return "$part1-$part2"
                }
                val stand = standaloneReg.find(line)
                if (stand != null) {
                    val candidate = stand.groupValues[1].trim().replace(" ", "").uppercase()
                    if (candidate.length in 5..10 && candidate.contains("-")) {
                        return candidate
                    }
                }
            }
        }

        // 2. Search entire text for standard NAFDAC format pattern
        val generalMatch = nafdacRegex.find(text)
        if (generalMatch != null) {
            val part1 = generalMatch.groupValues[1].uppercase()
            val part2 = generalMatch.groupValues[2].uppercase()
            return "$part1-$part2"
        }

        return null
    }
}
