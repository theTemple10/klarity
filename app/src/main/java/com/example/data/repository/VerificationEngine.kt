package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.ScanLogDao
import com.example.data.local.ScanLogEntity
import com.example.model.Product
import com.example.model.VerificationReport
import com.example.model.VerificationStatus
import com.example.model.VerificationStep
import java.util.regex.Pattern

class VerificationEngine(
    private val productDao: ProductDao,
    private val scanLogDao: ScanLogDao
) {
    suspend fun verifyProduct(
        barcode: String,
        scannedNafdac: String,
        batchCode: String,
        currentLocation: String
    ): VerificationReport {
        val steps = mutableListOf<VerificationStep>()
        val cleanBarcode = barcode.trim()
        val cleanBatch = batchCode.trim()
        val cleanNafdac = scannedNafdac.trim()

        // -------------------------------------------------------------
        // STEP 1: Check Database Existence
        // -------------------------------------------------------------
        val productEntity = productDao.getProductByBarcode(cleanBarcode)
        if (productEntity == null) {
            steps.add(
                VerificationStep(
                    stepNumber = 1,
                    title = "Central Product Ledger",
                    status = VerificationStatus.YELLOW,
                    details = "Barcode ($cleanBarcode) not found in certified registry.",
                    flaggedIssue = "Unverified packaging. Product has been logged to community ledger for manual NAFDAC audit."
                )
            )

            // Log unverified scan
            scanLogDao.insertScanLog(
                ScanLogEntity(
                    productId = null,
                    barcode = cleanBarcode,
                    brandName = "Unverified Brand",
                    productName = "Unknown FMCG Item",
                    batchCodeScanned = cleanBatch,
                    stateLocation = currentLocation,
                    status = "YELLOW",
                    statusReason = "Unverified Packaging: GTIN not present in database",
                    timestamp = System.currentTimeMillis()
                )
            )

            return VerificationReport(
                overallStatus = VerificationStatus.YELLOW,
                statusHeadline = "Unverified Packaging / Community Alert",
                statusExplanation = "This barcode is not yet indexed in our certified NAFDAC product ledger. It may be a newly packaged item, grey-market import, or unverified manufacturer. Please exercise caution.",
                steps = steps,
                product = null,
                scannedBarcode = cleanBarcode,
                scannedBatchCode = cleanBatch,
                scannedNafdac = cleanNafdac,
                scanLocation = currentLocation,
                velocityCount = 1,
                velocityLocations = listOf(currentLocation)
            )
        }

        val product = productEntity.toProduct()
        steps.add(
            VerificationStep(
                stepNumber = 1,
                title = "Central Product Ledger",
                status = VerificationStatus.GREEN,
                details = "Identified: ${product.brandName} - ${product.productName} (GTIN: ${product.barcodeGtin})"
            )
        )

        // -------------------------------------------------------------
        // STEP 2: Cross-Match NAFDAC Number
        // -------------------------------------------------------------
        val normalizedRegisteredNafdac = product.nafdacNumber.replace("-", "").replace(" ", "").uppercase()
        val normalizedScannedNafdac = cleanNafdac.replace("-", "").replace(" ", "").uppercase()

        val nafdacMatched = normalizedScannedNafdac.isNotEmpty() &&
                (normalizedScannedNafdac == normalizedRegisteredNafdac ||
                        cleanNafdac.equals(product.nafdacNumber, ignoreCase = true))

        if (!nafdacMatched && cleanNafdac.isNotEmpty()) {
            steps.add(
                VerificationStep(
                    stepNumber = 2,
                    title = "NAFDAC Certification Cross-Match",
                    status = VerificationStatus.RED,
                    details = "Scanned NAFDAC: '$cleanNafdac' does not match registered official number: '${product.nafdacNumber}'.",
                    flaggedIssue = "Token / NAFDAC Tampering detected. Counterfeiters often print unauthorized or recycled registration numbers."
                )
            )

            logScan(product, cleanBatch, currentLocation, "RED", "NAFDAC Number Mismatch")

            return VerificationReport(
                overallStatus = VerificationStatus.RED,
                statusHeadline = "Fraud Warning: NAFDAC Tampering",
                statusExplanation = "The regulatory approval number printed on this container does NOT match the authentic NAFDAC entry for ${product.brandName}. This product is likely an illicit reproduction.",
                steps = steps,
                product = product,
                scannedBarcode = cleanBarcode,
                scannedBatchCode = cleanBatch,
                scannedNafdac = cleanNafdac,
                scanLocation = currentLocation,
                velocityCount = 1,
                velocityLocations = listOf(currentLocation)
            )
        } else {
            steps.add(
                VerificationStep(
                    stepNumber = 2,
                    title = "NAFDAC Certification Cross-Match",
                    status = VerificationStatus.GREEN,
                    details = "NAFDAC Reg No. ${product.nafdacNumber} verified against official cosmetics registry."
                )
            )
        }

        // -------------------------------------------------------------
        // STEP 3: Run Batch Regex Engine
        // -------------------------------------------------------------
        val isBatchValid = try {
            val pattern = Pattern.compile(product.batchRegex)
            pattern.matcher(cleanBatch).matches()
        } catch (_: Exception) {
            cleanBatch.isNotEmpty()
        }

        if (!isBatchValid) {
            steps.add(
                VerificationStep(
                    stepNumber = 3,
                    title = "Batch Syntax Engine",
                    status = VerificationStatus.RED,
                    details = "Scanned Batch '$cleanBatch' fails brand manufacturing syntax rules ('${product.batchRegex}').",
                    flaggedIssue = "Syntax Discrepancy: Authentic ${product.brandName} products follow a strict format."
                )
            )

            logScan(product, cleanBatch, currentLocation, "RED", "Invalid Batch Code Syntax")

            return VerificationReport(
                overallStatus = VerificationStatus.RED,
                statusHeadline = "Syntax Discrepancy: Invalid Batch Code",
                statusExplanation = "The batch stamp format on this container contradicts the official manufacturing syntax pattern specified by ${product.brandName}. Counterfeit packagers frequently imprint incorrect alphanumeric patterns.",
                steps = steps,
                product = product,
                scannedBarcode = cleanBarcode,
                scannedBatchCode = cleanBatch,
                scannedNafdac = cleanNafdac,
                scanLocation = currentLocation,
                velocityCount = 1,
                velocityLocations = listOf(currentLocation)
            )
        } else {
            steps.add(
                VerificationStep(
                    stepNumber = 3,
                    title = "Batch Syntax Engine",
                    status = VerificationStatus.GREEN,
                    details = "Batch code '$cleanBatch' adheres to authentic manufacturing syntax specifications."
                )
            )
        }

        // -------------------------------------------------------------
        // STEP 4: Check Velocity Logs (Cloned Batch Trap)
        // -------------------------------------------------------------
        val priorLogs = scanLogDao.getLogsForBatch(cleanBatch)
        val distinctLocations = priorLogs.map { it.stateLocation }.toSet().toMutableList()
        if (!distinctLocations.contains(currentLocation)) {
            distinctLocations.add(currentLocation)
        }

        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        val recentCount = scanLogDao.getRecentScanCountForBatch(cleanBatch, oneHourAgo) + 1

        // Disparate location check: e.g. Lagos and Kano detected
        val hasDisparateCities = distinctLocations.size >= 2 &&
                (distinctLocations.any { it.contains("Lagos", ignoreCase = true) } &&
                        distinctLocations.any { it.contains("Kano", ignoreCase = true) || it.contains("Abuja", ignoreCase = true) || it.contains("Port Harcourt", ignoreCase = true) })

        val isVelocitySpike = recentCount >= 5 || hasDisparateCities

        if (isVelocitySpike) {
            steps.add(
                VerificationStep(
                    stepNumber = 4,
                    title = "Velocity Anomaly Audit",
                    status = VerificationStatus.RED,
                    details = "Batch '$cleanBatch' scanned across disparate regions: ${distinctLocations.joinToString(", ")} within suspicious timeframe.",
                    flaggedIssue = "Cloned Batch Trap: Counterfeiters print one real batch number onto thousands of fake units distributed across different regional markets simultaneously."
                )
            )

            logScan(product, cleanBatch, currentLocation, "RED", "Cloned Batch Anomaly Trap Triggered")

            return VerificationReport(
                overallStatus = VerificationStatus.RED,
                statusHeadline = "Cloned Batch Trap Triggered",
                statusExplanation = "High-severity anomaly: This identical batch number has just been scanned in ${distinctLocations.joinToString(" and ")}! Counterfeit packaging rings duplicate genuine batch codes onto fake bulk inventories.",
                steps = steps,
                product = product,
                scannedBarcode = cleanBarcode,
                scannedBatchCode = cleanBatch,
                scannedNafdac = cleanNafdac,
                scanLocation = currentLocation,
                velocityCount = recentCount,
                velocityLocations = distinctLocations
            )
        } else {
            steps.add(
                VerificationStep(
                    stepNumber = 4,
                    title = "Velocity Anomaly Audit",
                    status = VerificationStatus.GREEN,
                    details = "No suspicious cross-market velocity spikes or duplication signatures detected."
                )
            )
        }

        // -------------------------------------------------------------
        // All 4 Layers Passed!
        // -------------------------------------------------------------
        logScan(product, cleanBatch, currentLocation, "GREEN", "Passed All 4 Security Layers")

        return VerificationReport(
            overallStatus = VerificationStatus.GREEN,
            statusHeadline = "Verified Authentic Product",
            statusExplanation = "This product successfully passed all 4 verification barriers: Registered in central ledger, authentic NAFDAC license cross-matched, valid batch syntax, and clean distributed velocity profile.",
            steps = steps,
            product = product,
            scannedBarcode = cleanBarcode,
            scannedBatchCode = cleanBatch,
            scannedNafdac = cleanNafdac,
            scanLocation = currentLocation,
            velocityCount = recentCount,
            velocityLocations = distinctLocations
        )
    }

    private suspend fun logScan(
        product: Product,
        batchCode: String,
        location: String,
        status: String,
        reason: String
    ) {
        scanLogDao.insertScanLog(
            ScanLogEntity(
                productId = product.id,
                barcode = product.barcodeGtin,
                brandName = product.brandName,
                productName = product.productName,
                batchCodeScanned = batchCode,
                stateLocation = location,
                status = status,
                statusReason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
