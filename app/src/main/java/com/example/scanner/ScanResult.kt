package com.example.scanner

import android.graphics.Bitmap

data class ExtractedField(
    val value: String,
    val confidence: Float = 1.0f,
    val sourceIndex: Int = 0
)

data class ScanResult(
    val gtin: ExtractedField? = null,
    val nafdacNo: ExtractedField? = null,
    val batchCode: ExtractedField? = null,
    val expiryDate: ExtractedField? = null,
    val ingredients: List<String> = emptyList(),
    val rawText: String = ""
)

data class CapturedImage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bitmap: Bitmap,
    val parsedResult: ScanResult = ScanResult()
)
