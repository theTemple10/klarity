package com.example.scanner

object GtinValidator {

    fun isValid(code: String): Boolean {
        val clean = code.filter { it.isDigit() }
        if (clean.length !in listOf(8, 12, 13, 14)) return false

        val digits = clean.map { it.digitToInt() }
        val checkDigit = digits.last()
        val payload = digits.dropLast(1)

        // Right to left weighting: 3, 1, 3, 1...
        var sum = 0
        var multiplyByThree = true
        for (i in payload.indices.reversed()) {
            val weight = if (multiplyByThree) 3 else 1
            sum += payload[i] * weight
            multiplyByThree = !multiplyByThree
        }

        val calculated = (10 - (sum % 10)) % 10
        return calculated == checkDigit
    }
}
