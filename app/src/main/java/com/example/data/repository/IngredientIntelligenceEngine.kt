package com.example.data.repository

import android.util.Log
import com.example.data.ai.FirebaseGeminiGateway
import com.example.data.ai.GeminiGateway
import com.example.model.IngredientAction
import com.example.model.IngredientAnalysis
import com.example.model.IngredientDetail
import com.example.model.SafetyRating
import com.example.model.SkinProfile
import com.example.model.SkinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

class IngredientIntelligenceEngine(
    private val geminiGateway: GeminiGateway = FirebaseGeminiGateway()
) {

    // Toxic / Banned Ingredient Database in Cosmetics (NAFDAC & WHO Alerts)
    private val toxicDatabase = mapOf(
        "hydroquinone" to "Banned OTC substance. High risk of exogenous ochronosis and cellular toxicity.",
        "mercury" to "Severe heavy-metal neurotoxin banned globally in cosmetics.",
        "calomel" to "Hazardous mercurous chloride compound with severe systemic toxicity.",
        "clobetasol" to "Illegal potent topical steroid. Causes severe epidermal thinning and stretch marks.",
        "clobetasol propionate" to "Illegal prescription-only class 1 corticosteroid causing skin atrophy.",
        "betamethasone" to "Prescription fluorinated steroid causing cutaneous atrophy and hormone disruption.",
        "monobenzone" to "Aggressive medical depigmenting agent causing permanent irregular patchy skin death.",
        "fluocinonide" to "Prohibited high-potency topical steroid."
    )

    // Comedogenic and Clinical Database
    private val clinicalDatabase = mapOf(
        "aqua" to Pair(0, "Purified solvent base essential for water-soluble active delivery."),
        "water" to Pair(0, "Purified solvent base essential for water-soluble active delivery."),
        "glycerin" to Pair(0, "Potent humectant that draws ambient moisture into the epidermis."),
        "niacinamide" to Pair(0, "Vitamin B3: Superior barrier strengthener, fades dark spots, regulates sebum."),
        "ceramide np" to Pair(0, "Bio-identical lipid that reinforces moisture retention and repairs skin barrier."),
        "ceramide ap" to Pair(0, "Essential epidermal lipid preventing transepidermal water loss."),
        "ceramide eop" to Pair(0, "Critical structural ceramide protecting against environmental irritants."),
        "hyaluronic acid" to Pair(0, "Binds up to 1000x its weight in water, providing hydration without oiliness."),
        "sodium hyaluronate" to Pair(0, "Low molecular weight salt form of hyaluronic acid for deeper penetration."),
        "shea butter" to Pair(1, "Cold-pressed African botanical rich in stearic and oleic fatty acids."),
        "butyrospermum parkii" to Pair(1, "Pure African shea butter; anti-inflammatory and restorative for dry skin."),
        "jojoba oil" to Pair(2, "Plant wax ester structurally analogous to human sebum; balances oil production."),
        "simmondsia chinensis" to Pair(2, "Pure jojoba oil; lightweight conditioning agent."),
        "tocopherol" to Pair(1, "Vitamin E antioxidant neutralizing lipid peroxidation caused by sun and pollution."),
        "salicylic acid" to Pair(0, "Beta Hydroxy Acid (BHA) that penetrates inside pores to dissolve sebum."),
        "glycolic acid" to Pair(0, "Alpha Hydroxy Acid (AHA) that dissolves dead surface skin cells and brightens texture."),
        "arbutin" to Pair(0, "Gentle, safe tyrosinase inhibitor that fades dark spots safely."),
        "pearl extract" to Pair(0, "Natural amino acid and mineral complex promoting skin luminosity."),
        "sodium ascorbyl phosphate" to Pair(0, "Stable Vitamin C derivative with strong antioxidant and brightening power."),
        "paraffinum liquidum" to Pair(2, "Mineral oil occlusive; seals hydration but may feel heavy in tropical humidity."),
        "petrolatum" to Pair(2, "Gold-standard barrier sealant; highly effective for dry skin."),
        "dimethicone" to Pair(1, "Breathable silicone protective film smoothing texture and reducing irritation."),
        "parfum" to Pair(0, "Artificial fragrance mixture; known trigger for contact allergies and sensitized skin."),
        "fragrance" to Pair(0, "Synthetic fragrance blend; common sensitizer in sensitive skin."),
        "methylparaben" to Pair(0, "Antimicrobial preservative; safe in regulatory thresholds but potential sensitizer."),
        "isopropyl myristate" to Pair(5, "High-hazard comedogenic synthetic ester; known to trigger rapid breakouts in acne-prone skin."),
        "coconut oil" to Pair(4, "High comedogenic fatty oil; deeply occlusive but high risk of pore congestion."),
        "sodium lauryl sulfate" to Pair(0, "Harsh surfactant that can strip natural skin lipids.")
    )

    suspend fun analyzeIngredients(
        rawIngredients: List<String>,
        skinProfile: SkinProfile
    ): IngredientAnalysis = withContext(Dispatchers.IO) {
        val deterministicResult = evaluateLocalDermatologyEngine(rawIngredients, skinProfile)

        // Run cloud AI analysis through GeminiGateway
        try {
            val ingredientString = rawIngredients.joinToString(", ")
            val profileString = "${skinProfile.skinType.displayName}, Acne-Prone: ${skinProfile.isAcneProne}, Sensitive: ${skinProfile.isSensitive}, Hyperpigmentation: ${skinProfile.hasHyperpigmentation}, Climate: ${skinProfile.climateZone}"

            val systemPrompt = """
                You are a cosmetic dermatologist and formulation analyst specializing in African skin health and cosmetic safety.
                Analyze the following list of cosmetic ingredients for this user's skin profile in tropical Nigeria.
                Return a strict JSON object with this exact schema:
                {
                  "summary": "Short 2-line summary of dermatological impact.",
                  "overallScore": "EXCELLENT" | "MODERATE" | "USE_WITH_CAUTION" | "DANGEROUS",
                  "toxicWarnings": ["warning text if any toxic bleaching agent or steroid found"]
                }
                Do not include markdown code block ticks, only raw valid JSON.
            """.trimIndent()

            val prompt = "Ingredients: $ingredientString\nSkin Profile: $profileString"
            val aiResult = geminiGateway.generateText(prompt, systemPrompt)
            if (aiResult.isSuccess) {
                val jsonText = aiResult.getOrNull()
                if (!jsonText.isNullOrBlank()) {
                    val parsed = parseJsonSummary(jsonText)
                    if (parsed != null) {
                        // Merge with deterministic safety checks (local toxic check ALWAYS wins)
                        val combinedWarnings = (deterministicResult.toxicWarnings + parsed.second).distinct()
                        val worseScore = if (combinedWarnings.isNotEmpty()) {
                            SafetyRating.DANGEROUS
                        } else if (deterministicResult.overallScore == SafetyRating.DANGEROUS.name || parsed.first == SafetyRating.DANGEROUS) {
                            SafetyRating.DANGEROUS
                        } else if (deterministicResult.overallScore == SafetyRating.USE_WITH_CAUTION.name || parsed.first == SafetyRating.USE_WITH_CAUTION) {
                            SafetyRating.USE_WITH_CAUTION
                        } else if (deterministicResult.overallScore == SafetyRating.MODERATE.name || parsed.first == SafetyRating.MODERATE) {
                            SafetyRating.MODERATE
                        } else {
                            SafetyRating.EXCELLENT
                        }

                        return@withContext deterministicResult.copy(
                            overallScore = worseScore.name,
                            summary = parsed.third.ifBlank { deterministicResult.summary },
                            toxicWarnings = combinedWarnings
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("IngredientEngine", "AI analysis skipped: ${e.message}")
        }

        deterministicResult
    }

    private fun parseJsonSummary(rawJson: String): Triple<SafetyRating, List<String>, String>? {
        return try {
            val clean = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)
            val scoreStr = obj.optString("overallScore", "MODERATE")
            val score = try { SafetyRating.valueOf(scoreStr) } catch (_: Exception) { SafetyRating.MODERATE }
            val warningsArray = obj.optJSONArray("toxicWarnings")
            val warnings = mutableListOf<String>()
            if (warningsArray != null) {
                for (i in 0 until warningsArray.length()) {
                    warnings.add(warningsArray.getString(i))
                }
            }
            val summary = obj.optString("summary", "")
            Triple(score, warnings, summary)
        } catch (_: Exception) {
            null
        }
    }

    fun evaluateLocalDermatologyEngine(
        rawIngredients: List<String>,
        profile: SkinProfile
    ): IngredientAnalysis {
        val details = mutableListOf<IngredientDetail>()
        val toxicWarnings = mutableListOf<String>()
        var highestComedogenic = 0
        var flaggedPoreCloggers = 0

        for (raw in rawIngredients) {
            val normalized = raw.trim().lowercase(Locale.ROOT)
            if (normalized.isBlank()) continue

            // 1. Check Toxic Additive Database
            val toxicMatch = toxicDatabase.entries.firstOrNull { normalized.contains(it.key) }
            if (toxicMatch != null) {
                toxicWarnings.add("Found ${raw.trim()}: ${toxicMatch.value}")
                details.add(
                    IngredientDetail(
                        name = raw.trim(),
                        comedogenicScore = 0,
                        action = IngredientAction.TOXIC_ALERT,
                        explanation = toxicMatch.value
                    )
                )
                continue
            }

            // 2. Check Clinical Database
            val clinicalMatch = clinicalDatabase.entries.firstOrNull { normalized.contains(it.key) }
            if (clinicalMatch != null) {
                val score = clinicalMatch.value.first
                val expl = clinicalMatch.value.second

                if (score > highestComedogenic) highestComedogenic = score
                if (score >= 3) flaggedPoreCloggers++

                val action = when {
                    score >= 4 -> IngredientAction.CON
                    score == 3 && profile.isAcneProne -> IngredientAction.CON
                    normalized.contains("niacinamide") || normalized.contains("ceramide") ||
                            normalized.contains("hyaluronic") || normalized.contains("arbutin") ||
                            normalized.contains("salicylic") || normalized.contains("ascorbyl") -> IngredientAction.PRO
                    else -> IngredientAction.NEUTRAL
                }

                details.add(
                    IngredientDetail(
                        name = raw.trim(),
                        comedogenicScore = score,
                        action = action,
                        explanation = expl
                    )
                )
            } else {
                details.add(
                    IngredientDetail(
                        name = raw.trim(),
                        comedogenicScore = 0,
                        action = IngredientAction.NEUTRAL,
                        explanation = "Standard formulation compound."
                    )
                )
            }
        }

        val overallScore = when {
            toxicWarnings.isNotEmpty() -> SafetyRating.DANGEROUS
            flaggedPoreCloggers > 0 && profile.isAcneProne -> SafetyRating.USE_WITH_CAUTION
            highestComedogenic >= 4 -> SafetyRating.USE_WITH_CAUTION
            highestComedogenic >= 2 -> SafetyRating.MODERATE
            else -> SafetyRating.EXCELLENT
        }

        val summary = when (overallScore) {
            SafetyRating.DANGEROUS -> "Critical hazard: prohibited bleaching or corticosteroid additives found."
            SafetyRating.USE_WITH_CAUTION -> "Contains pore-clogging esters that may cause breakouts in humid weather."
            SafetyRating.MODERATE -> "Balanced cosmetic formula suitable for standard skin with mild occlusives."
            SafetyRating.EXCELLENT -> "Skin-barrier supportive formula with low comedogenic risk."
        }

        return IngredientAnalysis(
            summary = summary,
            overallScore = overallScore.name,
            ingredients = details,
            toxicWarnings = toxicWarnings,
            skinTypeMatch = "${profile.skinType.displayName} match",
            comedogenicRating = highestComedogenic,
            climateSuitability = "${profile.climateZone} compatible"
        )
    }
}
