package com.example.model

enum class VerificationStatus {
    GREEN,   // Verified Product
    YELLOW,  // Unverified Packaging / Logged to Community
    RED      // Alert / Counterfeit Discrepancy
}

enum class SkinType(val displayName: String, val description: String) {
    OILY("Oily Skin", "Excess sebum, enlarged pores, prone to shine"),
    DRY("Dry Skin", "Flaky, tight, lacks natural moisture and lipids"),
    COMBINATION("Combination Skin", "Oily T-zone (forehead, nose), dry cheeks"),
    SENSITIVE("Sensitive Skin", "Easily irritated, reactive to fragrance, stinging"),
    NORMAL("Balanced / Normal", "Well-hydrated, even texture, minimal sensitivity")
}

data class SkinProfile(
    val skinType: SkinType = SkinType.OILY,
    val isAcneProne: Boolean = true,
    val hasHyperpigmentation: Boolean = true,
    val isSensitive: Boolean = false,
    val climateZone: String = "Tropical Humid (Lagos / Southern Nigeria)",
    val allergens: List<String> = listOf("Artificial Fragrance", "Parabens")
)

data class Product(
    val id: String,
    val barcodeGtin: String,
    val brandName: String,
    val productName: String,
    val isLocalBrand: Boolean,
    val nafdacNumber: String,
    val batchRegex: String,
    val ingredients: List<String>,
    val imageUrl: String = "",
    val category: String = "Personal Care & Skincare",
    val description: String = ""
)

data class VerificationStep(
    val stepNumber: Int,
    val title: String,
    val status: VerificationStatus,
    val details: String,
    val flaggedIssue: String? = null
)

data class VerificationReport(
    val overallStatus: VerificationStatus,
    val statusHeadline: String,
    val statusExplanation: String,
    val steps: List<VerificationStep>,
    val product: Product?,
    val scannedBarcode: String,
    val scannedBatchCode: String,
    val scannedNafdac: String,
    val scanLocation: String,
    val velocityCount: Int,
    val velocityLocations: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class IngredientDetail(
    val name: String,
    val action: String, // "PRO", "CON", "NEUTRAL", "TOXIC_ALERT"
    val explanation: String,
    val comedogenicScore: Int? = null, // 0 to 5
    val isToxicBleach: Boolean = false
)

data class IngredientAnalysis(
    val summary: String,
    val overallScore: String, // "EXCELLENT", "MODERATE", "USE_WITH_CAUTION", "DANGEROUS"
    val ingredients: List<IngredientDetail>,
    val toxicWarnings: List<String> = emptyList(),
    val skinTypeMatch: String = ""
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class CounterfeitReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productName: String,
    val brandName: String,
    val batchCode: String,
    val marketName: String,
    val city: String,
    val sellerDetails: String,
    val timestamp: Long = System.currentTimeMillis()
)
