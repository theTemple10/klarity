package com.example.data.repository

import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.model.IngredientAnalysis
import com.example.model.IngredientDetail
import com.example.model.SkinProfile
import com.example.model.SkinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

class IngredientIntelligenceEngine {

    // Toxic / Banned Ingredient Database in Cosmetics (NAFDAC & WHO Alerts)
    private val toxicDatabase = mapOf(
        "hydroquinone" to "Banned OTC substance. High risk of exogenous ochronosis (irreversible skin discoloration) and cellular toxicity.",
        "mercury" to "NAFDAC Alert: Severe heavy-metal neurotoxin and nephrotoxic compound banned globally in cosmetics.",
        "calomel" to "Hazardous mercurous chloride compound with severe systemic toxicity.",
        "clobetasol" to "Illegal potent topical steroid. Causes severe epidermal thinning, stretch marks, and adrenal suppression.",
        "clobetasol propionate" to "Illegal prescription-only class 1 corticosteroid. Causes severe skin atrophy and vascular damage.",
        "betamethasone" to "Prescription fluorinated steroid causing cutaneous atrophy and hormone disruption.",
        "monobenzone" to "Aggressive medical depigmenting agent causing permanent irregular patchy skin death.",
        "fluocinonide" to "Prohibited high-potency topical steroid."
    )

    // Comedogenic and Clinical Database
    private val clinicalDatabase = mapOf(
        "aqua" to Pair(0, "Purified solvent base essential for water-soluble active delivery."),
        "water" to Pair(0, "Purified solvent base essential for water-soluble active delivery."),
        "glycerin" to Pair(0, "Potent humectant that draws ambient moisture into the epidermis without clogging pores."),
        "niacinamide" to Pair(0, "Vitamin B3: Superior barrier strengthener, fades post-inflammatory hyperpigmentation, and regulates sebum."),
        "ceramide np" to Pair(0, "Bio-identical lipid that reinforces moisture retention and repairs compromised skin barrier."),
        "ceramide ap" to Pair(0, "Essential epidermal lipid preventing transepidermal water loss (TEWL)."),
        "ceramide eop" to Pair(0, "Critical structural ceramide protecting against environmental irritants."),
        "hyaluronic acid" to Pair(0, "Binds up to 1000x its weight in water, providing plumpness without oiliness."),
        "sodium hyaluronate" to Pair(0, "Low molecular weight salt form of hyaluronic acid for deeper penetration."),
        "shea butter" to Pair(1, "Cold-pressed African botanical rich in stearic & oleic fatty acids; deeply nourishing."),
        "butyrospermum parkii" to Pair(1, "Pure African shea butter; anti-inflammatory and restorative for dry or damaged skin."),
        "jojoba oil" to Pair(2, "Plant wax ester structurally analogous to human sebum; balances oil production."),
        "simmondsia chinensis" to Pair(2, "Pure jojoba oil; lightweight conditioning agent."),
        "tocopherol" to Pair(1, "Vitamin E antioxidant neutralizing lipid peroxidation caused by sun and pollution."),
        "salicylic acid" to Pair(0, "Beta Hydroxy Acid (BHA) that penetrates inside pores to dissolve trapped sebum and bacteria."),
        "glycolic acid" to Pair(0, "Alpha Hydroxy Acid (AHA) that dissolves dead surface skin cells and brightens texture."),
        "arbutin" to Pair(0, "Gentle, safe, NAFDAC-compliant tyrosinase inhibitor that fades dark spots safely."),
        "pearl extract" to Pair(0, "Natural amino acid and mineral complex promoting skin luminosity."),
        "sodium ascorbyl phosphate" to Pair(0, "Stable Vitamin C derivative with strong antioxidant and brightening power."),
        "paraffinum liquidum" to Pair(2, "Mineral oil occlusive; seals hydration but may feel heavy in tropical humidity."),
        "petrolatum" to Pair(2, "Gold-standard barrier sealant; highly effective for dry skin, potentially heavy for acne-prone skin."),
        "dimethicone" to Pair(1, "Breathable silicone protective film smoothing texture and reducing irritation."),
        "parfum" to Pair(0, "Artificial fragrance mixture; known trigger for contact allergies and sensitized skin."),
        "fragrance" to Pair(0, "Synthetic fragrance blend; common sensitizer in sensitive or eczema-prone skin."),
        "methylparaben" to Pair(0, "Antimicrobial preservative; safe in regulatory thresholds but potential sensitizer for hyper-reactive skin."),
        "isopropyl myristate" to Pair(5, "High-hazard comedogenic synthetic ester; known to trigger rapid cystic acne in acne-prone skin."),
        "coconut oil" to Pair(4, "High comedogenic fatty oil; deeply occlusive but high risk of pore congestion on face."),
        "sodium lauryl sulfate" to Pair(0, "Harsh surfactant that can strip natural skin lipids and disrupt epidermal barrier.")
    )

    suspend fun analyzeIngredients(
        rawIngredients: List<String>,
        skinProfile: SkinProfile
    ): IngredientAnalysis = withContext(Dispatchers.IO) {
        // First check for online Gemini API
        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiIngredientApi(rawIngredients, skinProfile, apiKey)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                // Fall back gracefully to deterministic clinical toxicologist engine
            }
        }

        // Deterministic Clinical Toxicologist Engine
        return@withContext evaluateLocalDermatologyEngine(rawIngredients, skinProfile)
    }

    private suspend fun callGeminiIngredientApi(
        ingredients: List<String>,
        profile: SkinProfile,
        apiKey: String
    ): IngredientAnalysis? {
        val ingredientString = ingredients.joinToString(", ")
        val profileString = "${profile.skinType.displayName}, Acne-Prone: ${profile.isAcneProne}, Sensitive: ${profile.isSensitive}, Hyperpigmentation: ${profile.hasHyperpigmentation}, Climate: ${profile.climateZone}"

        val systemPrompt = "You are an expert cosmetic dermatologist and clinical toxicologist specializing in African and melanin-rich skin health and Nigerian cosmetic safety regulations. Analyze the following list of extracted ingredients alongside the user's skin profile.\n" +
                "User Skin Profile: $profileString\n" +
                "Extracted Ingredients List: $ingredientString\n\n" +
                "Return a strict JSON object with this exact schema:\n" +
                "{\n" +
                "  \"summary\": \"Short human-centric summary of how this product impacts this skin type in tropical climates.\",\n" +
                "  \"overallScore\": \"EXCELLENT\" | \"MODERATE\" | \"USE_WITH_CAUTION\" | \"DANGEROUS\",\n" +
                "  \"toxicWarnings\": [\"warning details if any banned bleaching agent or steroid found\"],\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"name\": \"Ingredient Name\",\n" +
                "      \"action\": \"PRO\" | \"CON\" | \"NEUTRAL\" | \"TOXIC_ALERT\",\n" +
                "      \"explanation\": \"Clear dermatological impact explanation.\",\n" +
                "      \"comedogenicScore\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Do not include markdown ticks like ```json, just pure raw JSON."

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "Analyze ingredients: $ingredientString for profile: $profileString")
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2f,
                maxOutputTokens = 2048
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        // Use gemini-3.8-flash as primary
        val response = try {
            RetrofitClient.geminiService.generateContent("gemini-3.8-flash", apiKey, request)
        } catch (_: Exception) {
            RetrofitClient.geminiService.generateContent("gemini-3.5-flash", apiKey, request)
        }

        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return null
        return parseJsonAnalysis(jsonText, profile)
    }

    private fun parseJsonAnalysis(rawJson: String, profile: SkinProfile): IngredientAnalysis? {
        return try {
            val cleanJson = rawJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonObject = JSONObject(cleanJson)
            val summary = jsonObject.optString("summary", "Ingredient safety assessment complete.")
            val overallScore = jsonObject.optString("overallScore", "MODERATE")
            val toxicArray = jsonObject.optJSONArray("toxicWarnings")
            val toxicWarnings = mutableListOf<String>()
            if (toxicArray != null) {
                for (i in 0 until toxicArray.length()) {
                    toxicWarnings.add(toxicArray.getString(i))
                }
            }

            val ingredientsArray = jsonObject.getJSONArray("ingredients")
            val details = mutableListOf<IngredientDetail>()
            for (i in 0 until ingredientsArray.length()) {
                val item = ingredientsArray.getJSONObject(i)
                val name = item.getString("name")
                val action = item.optString("action", "NEUTRAL")
                val explanation = item.optString("explanation", "")
                val comedogenic = if (item.has("comedogenicScore")) item.optInt("comedogenicScore") else null
                val isToxic = action == "TOXIC_ALERT"

                details.add(
                    IngredientDetail(
                        name = name,
                        action = action,
                        explanation = explanation,
                        comedogenicScore = comedogenic,
                        isToxicBleach = isToxic
                    )
                )
            }

            IngredientAnalysis(
                summary = summary,
                overallScore = overallScore,
                ingredients = details,
                toxicWarnings = toxicWarnings,
                skinTypeMatch = profile.skinType.displayName
            )
        } catch (e: Exception) {
            null
        }
    }

    fun evaluateLocalDermatologyEngine(
        rawIngredients: List<String>,
        profile: SkinProfile
    ): IngredientAnalysis {
        val details = mutableListOf<IngredientDetail>()
        val toxicWarnings = mutableListOf<String>()
        var conCount = 0
        var proCount = 0
        var hasToxic = false

        for (raw in rawIngredients) {
            val cleanName = raw.trim()
            val lower = cleanName.lowercase(Locale.ROOT)

            // 1. Check Toxic / Banned Compounds
            var toxicMatched: String? = null
            for ((toxicKey, toxicAlert) in toxicDatabase) {
                if (lower.contains(toxicKey)) {
                    toxicMatched = toxicAlert
                    break
                }
            }

            if (toxicMatched != null) {
                hasToxic = true
                toxicWarnings.add("TOXIC ALERT: Contains $cleanName. $toxicMatched")
                details.add(
                    IngredientDetail(
                        name = cleanName,
                        action = "TOXIC_ALERT",
                        explanation = toxicMatched,
                        comedogenicScore = 0,
                        isToxicBleach = true
                    )
                )
                continue
            }

            // 2. Clinical and Comedogenic Lookup
            var matchedEntry: Pair<Int, String>? = null
            for ((key, pair) in clinicalDatabase) {
                if (lower.contains(key)) {
                    matchedEntry = pair
                    break
                }
            }

            val comedogenic = matchedEntry?.first ?: 0
            val baseDesc = matchedEntry?.second ?: "Functional cosmetic formulation ingredient."

            // Evaluate impact on specific skin type
            val (action, personalizedExplanation) = evaluateSkinImpact(cleanName, lower, comedogenic, baseDesc, profile)

            if (action == "PRO") proCount++
            if (action == "CON") conCount++

            details.add(
                IngredientDetail(
                    name = cleanName,
                    action = action,
                    explanation = personalizedExplanation,
                    comedogenicScore = comedogenic,
                    isToxicBleach = false
                )
            )
        }

        val overallScore = when {
            hasToxic -> "DANGEROUS"
            conCount > proCount -> "USE_WITH_CAUTION"
            proCount > 2 -> "EXCELLENT"
            else -> "MODERATE"
        }

        val summary = when {
            hasToxic -> "CRITICAL SAFETY ALERT: This formula contains prohibited bleaching agents or potent steroids banned by NAFDAC. Do not apply to skin."
            profile.skinType == SkinType.OILY && profile.isAcneProne && details.any { (it.comedogenicScore ?: 0) >= 4 } ->
                "Contains heavy occlusive esters with high comedogenic ratings (scores 4-5). May trigger acne flare-ups and congested pores for ${profile.skinType.displayName} in tropical climates."
            profile.hasHyperpigmentation && details.any { it.name.contains("Niacinamide", ignoreCase = true) || it.name.contains("Arbutin", ignoreCase = true) } ->
                "Highly beneficial for melanin-rich skin: Features clinically proven tyrosinase inhibitors and barrier-restoring niacinamide to fade hyperpigmentation safely."
            profile.skinType == SkinType.DRY ->
                "Rich emollient matrix providing lipid reinforcement and intense hydration to restore dry or flaky skin barriers."
            else ->
                "Well-balanced cosmetic formulation suitable for daily maintenance for ${profile.skinType.displayName}."
        }

        return IngredientAnalysis(
            summary = summary,
            overallScore = overallScore,
            ingredients = details,
            toxicWarnings = toxicWarnings,
            skinTypeMatch = profile.skinType.displayName
        )
    }

    private fun evaluateSkinImpact(
        name: String,
        lower: String,
        comedogenic: Int,
        baseDesc: String,
        profile: SkinProfile
    ): Pair<String, String> {
        // High comedogenic rating on oily/acne-prone skin
        if (comedogenic >= 4 && (profile.skinType == SkinType.OILY || profile.isAcneProne)) {
            return Pair(
                "CON",
                "High comedogenic score ($comedogenic/5). Clogs pores and accelerates micro-comedone formation on oily or acne-prone skin."
            )
        }

        // Fragrance / alcohol on sensitive skin
        if ((lower.contains("fragrance") || lower.contains("parfum") || lower.contains("alcohol denat")) && profile.isSensitive) {
            return Pair(
                "CON",
                "Volatile scent compound. Known to trigger stinging, erythema, and barrier compromise in sensitive skin."
            )
        }

        // Active beneficial ingredients
        if (lower.contains("niacinamide")) {
            val explanation = if (profile.hasHyperpigmentation) {
                "PRO: Reduces melanosome transfer, accelerating fading of stubborn dark spots and sun-induced hyperpigmentation."
            } else {
                "PRO: Strengthens ceramide synthesis and regulates excess sebum excretion in hot climates."
            }
            return Pair("PRO", explanation)
        }

        if (lower.contains("ceramide")) {
            return Pair("PRO", "PRO: Essential stratum corneum lipid that locks in moisture and guards against humidity fluctuations.")
        }

        if (lower.contains("hyaluronic") || lower.contains("hyaluronate")) {
            return Pair("PRO", "PRO: Superior humectant that provides weightless, non-greasy plumping hydration.")
        }

        if (lower.contains("shea butter") || lower.contains("butyrospermum")) {
            return if (profile.skinType == SkinType.DRY) {
                Pair("PRO", "PRO: Superb African botanical that heals lipid-depleted skin and locks in deep nourishment.")
            } else {
                Pair("NEUTRAL", "Rich botanical lipid with low-moderate pore risk (rating 1-2). Best used moderately on oily zones.")
            }
        }

        if (lower.contains("arbutin")) {
            return Pair("PRO", "PRO: Safe, NAFDAC-compliant tyrosinase inhibitor for evening skin tone without toxic bleaching.")
        }

        if (lower.contains("salicylic")) {
            return Pair("PRO", "PRO: Lipophilic beta-hydroxy acid that dissolves keratin plugs inside follicles.")
        }

        return Pair("NEUTRAL", baseDesc)
    }
}
