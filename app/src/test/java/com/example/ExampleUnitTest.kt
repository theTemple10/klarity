package com.example

import com.example.scanner.GtinValidator
import com.example.scanner.NafdacParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNiveaGtinValidation() {
        val barcode = "4005900925022"
        assertTrue("Barcode 4005900925022 must be a valid GTIN-13", GtinValidator.isValid(barcode))
    }

    @Test
    fun testNafdacParserWithSixDigits() {
        val textWithNafdac = "MANUFACTURED BY BEIERSDORF AG. NAFDAC REG NO: A2-101409. STORE IN COOL DRY PLACE."
        val parsed = NafdacParser.parse(textWithNafdac)
        assertNotNull("Should parse NAFDAC number with 6 digits", parsed)
        assertEquals("A2-101409", parsed)
    }

    @Test
    fun testNafdacParserVariants() {
        assertEquals("A2-101409", NafdacParser.parse("NAFDAC: A2-101409"))
        assertEquals("02-1988", NafdacParser.parse("Reg No. 02-1988"))
        assertEquals("B4-1102", NafdacParser.parse("NAFDAC REG: B4-1102"))
    }
}
