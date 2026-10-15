package com.example.scanner

object NafdacParser {

    // Matches e.g. A2-101409, 02-1988, A4-2281, B4-1102, 04-123456, A2/101409, A2 101409
    private val nafdacRegex = Regex("""\b([A-Za-z0-9]{1,4})\s*[-–/]\s*([A-Za-z0-9]{3,8})\b""")
    private val standaloneReg = Regex("""(?i)\b(?:NAFDAC|REG(?:\.|\s*NO)?|N\.R\.N)\s*[:.]?\s*([A-Za-z0-9\-–/\s]{4,14})""")

    fun parse(text: String): String? {
        val lines = text.lines()

        // 1. First look on lines containing NAFDAC or REG keyword
        for (line in lines) {
            if (line.contains("NAFDAC", ignoreCase = true) || line.contains("REG NO", ignoreCase = true)) {
                val match = nafdacRegex.find(line)
                if (match != null) {
                    val part1 = match.groupValues[1].uppercase()
                    val part2 = match.groupValues[2].uppercase()
                    return "$part1-$part2"
                }
                val stand = standaloneReg.find(line)
                if (stand != null) {
                    val candidate = stand.groupValues[1].trim().replace(" ", "").replace("/", "-").uppercase()
                    if (candidate.length in 5..14 && candidate.contains("-")) {
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
