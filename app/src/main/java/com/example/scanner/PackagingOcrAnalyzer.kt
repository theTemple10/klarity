package com.example.scanner

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object PackagingOcrAnalyzer {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    suspend fun analyzeBitmap(bitmap: Bitmap, imageIndex: Int = 0): ScanResult {
        var gtinField: ExtractedField? = null
        var nafdacField: ExtractedField? = null
        var batchField: ExtractedField? = null
        var expiryField: ExtractedField? = null
        var ingredientsList: List<String> = emptyList()
        var extractedRawText = ""

        val inputImage = InputImage.fromBitmap(bitmap, 0)

        // 1. Scan for barcodes / DataMatrix in image
        try {
            val barcodes = barcodeScanner.process(inputImage).await()
            for (barcode in barcodes) {
                val rawValue = barcode.rawValue ?: continue
                if (barcode.format == Barcode.FORMAT_DATA_MATRIX || barcode.format == Barcode.FORMAT_QR_CODE) {
                    val gs1 = Gs1Parser.parse(rawValue)
                    if (gs1.gtin != null && GtinValidator.isValid(gs1.gtin)) {
                        gtinField = ExtractedField(gs1.gtin, 1.0f, imageIndex)
                    }
                    if (gs1.batch != null) {
                        batchField = ExtractedField(gs1.batch, 1.0f, imageIndex)
                    }
                    if (gs1.expiry != null) {
                        expiryField = ExtractedField(gs1.expiry, 1.0f, imageIndex)
                    }
                } else {
                    if (GtinValidator.isValid(rawValue)) {
                        gtinField = ExtractedField(rawValue, 1.0f, imageIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PackagingOcr", "Barcode scan exception: ${e.message}")
        }

        // 2. Perform OCR on still image (CAPTURE_MODE_MAXIMIZE_QUALITY)
        try {
            val visionText = textRecognizer.process(inputImage).await()
            extractedRawText = visionText.text

            // If GTIN wasn't found via barcode, see if text has a barcode number
            if (gtinField == null) {
                for (line in visionText.text.lines()) {
                    val digitsOnly = line.filter { it.isDigit() }
                    if (digitsOnly.length in listOf(8, 12, 13) && GtinValidator.isValid(digitsOnly)) {
                        gtinField = ExtractedField(digitsOnly, 0.85f, imageIndex)
                        break
                    }
                }
            }

            // Extract NAFDAC
            val nafdacParsed = NafdacParser.parse(extractedRawText)
            if (nafdacParsed != null) {
                nafdacField = ExtractedField(nafdacParsed, 0.90f, imageIndex)
            }

            // Extract Batch
            val batchParsed = BatchParser.parse(extractedRawText)
            if (batchParsed != null && batchField == null) {
                batchField = ExtractedField(batchParsed, 0.85f, imageIndex)
            }

            // Extract Expiry
            val expiryParsed = ExpiryParser.parse(extractedRawText)
            if (expiryParsed != null && expiryField == null) {
                expiryField = ExtractedField(expiryParsed, 0.85f, imageIndex)
            }

            // Extract Ingredients
            val parsedIngredients = InciParser.parse(extractedRawText)
            if (parsedIngredients.size >= 3) {
                ingredientsList = parsedIngredients
            }
        } catch (e: Exception) {
            Log.w("PackagingOcr", "OCR processing exception: ${e.message}")
        }

        return ScanResult(
            gtin = gtinField,
            nafdacNo = nafdacField,
            batchCode = batchField,
            expiryDate = expiryField,
            ingredients = ingredientsList,
            rawText = extractedRawText
        )
    }

    /**
     * CameraX ImageAnalysis analyzer for live barcode detection
     */
    class LiveBarcodeAnalyzer(
        private val onBarcodeDetected: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private var lastDetectedTime = 0L

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val now = System.currentTimeMillis()
                        if (now - lastDetectedTime > 1000) { // 1 sec debounce
                            for (barcode in barcodes) {
                                val raw = barcode.rawValue ?: continue
                                if (barcode.format == Barcode.FORMAT_DATA_MATRIX || barcode.format == Barcode.FORMAT_QR_CODE) {
                                    val gs1 = Gs1Parser.parse(raw)
                                    val candidate = gs1.gtin ?: raw
                                    if (GtinValidator.isValid(candidate) || (candidate.length in 8..14 && candidate.all { it.isDigit() })) {
                                        lastDetectedTime = now
                                        onBarcodeDetected(candidate)
                                        break
                                    }
                                } else {
                                    val digits = raw.filter { it.isDigit() }
                                    if (GtinValidator.isValid(digits) || (digits.length in 8..14)) {
                                        lastDetectedTime = now
                                        onBarcodeDetected(digits)
                                        break
                                    }
                                }
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
}
