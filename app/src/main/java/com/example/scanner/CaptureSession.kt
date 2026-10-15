package com.example.scanner

import android.graphics.Bitmap

class CaptureSession(val maxCaptures: Int = 6) {

    private val _captures = mutableListOf<CapturedImage>()
    val captures: List<CapturedImage> get() = _captures.toList()

    fun addCapture(bitmap: Bitmap, parsedResult: ScanResult): Boolean {
        if (_captures.size >= maxCaptures) return false
        _captures.add(CapturedImage(bitmap = bitmap, parsedResult = parsedResult))
        return true
    }

    fun removeCaptureAt(index: Int) {
        if (index in _captures.indices) {
            _captures.removeAt(index)
        }
    }

    fun clear() {
        _captures.clear()
    }

    fun mergeResults(): ScanResult {
        if (_captures.isEmpty()) return ScanResult()

        // 1. Merge GTIN
        val gtinCandidates = _captures.mapNotNull { it.parsedResult.gtin }
        val bestGtin = gtinCandidates.groupBy { it.value }
            .maxByOrNull { it.value.size }
            ?.value?.firstOrNull()

        // 2. Merge NAFDAC
        val nafdacCandidates = _captures.mapNotNull { it.parsedResult.nafdacNo }
        val bestNafdac = nafdacCandidates.groupBy { it.value }
            .maxByOrNull { it.value.size }
            ?.value?.firstOrNull()

        // 3. Merge Batch
        val batchCandidates = _captures.mapNotNull { it.parsedResult.batchCode }
        val bestBatch = batchCandidates.groupBy { it.value }
            .maxByOrNull { it.value.size }
            ?.value?.firstOrNull()

        // 4. Merge Expiry
        val expiryCandidates = _captures.mapNotNull { it.parsedResult.expiryDate }
        val bestExpiry = expiryCandidates.groupBy { it.value }
            .maxByOrNull { it.value.size }
            ?.value?.firstOrNull()

        // 5. Merge Ingredients (take longest list or union of tokens)
        val allIngredients = _captures.flatMap { it.parsedResult.ingredients }
            .distinctBy { it.lowercase() }

        val combinedRawText = _captures.joinToString("\n---\n") { it.parsedResult.rawText }

        return ScanResult(
            gtin = bestGtin,
            nafdacNo = bestNafdac,
            batchCode = bestBatch,
            expiryDate = bestExpiry,
            ingredients = allIngredients,
            rawText = combinedRawText
        )
    }
}
