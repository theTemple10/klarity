package com.example.data.repository

import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.model.ChatMessage
import com.example.model.SkinProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DermAIChatRepository {

    private val conversationHistory = mutableListOf<ChatMessage>()

    val systemInstruction = """
        You are 'Klarity DermAI', an elite consultant cosmetic dermatologist and clinical toxicologist specializing in African skin health, tropical dermatology, and anti-counterfeit cosmetic vigilance in Nigeria and emerging markets.
        Your tone is authoritative, warm, scientifically rigorous, and accessible.
        Key areas of your expertise:
        1. Explaining the dangers of illicit skin-lightening additives (hydroquinone, clobetasol steroids, mercury salts) and how to identify counterfeit beauty products in markets like Balogun, Idumota, Trade Fair, Kantin Kwari, and Onitsha.
        2. Formulating safe, effective skincare routines for melanin-rich skin targeting post-inflammatory hyperpigmentation (PIH), acne, and sun protection without white cast.
        3. Clarifying regulatory status with NAFDAC (National Agency for Food and Drug Administration and Control) and verifying batch integrity.
        Keep answers concise, actionable, and formatted with bullet points for easy scanning on mobile screens.
    """.trimIndent()

    suspend fun sendMessage(
        userMessage: String,
        skinProfile: SkinProfile
    ): ChatMessage = withContext(Dispatchers.IO) {
        val userChat = ChatMessage(
            sender = "user",
            text = userMessage
        )
        conversationHistory.add(userChat)

        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Build multi-turn contents
                val contents = conversationHistory.takeLast(8).map { msg ->
                    GeminiContent(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                }

                val profileContext = "User Profile: ${skinProfile.skinType.displayName}, Acne: ${skinProfile.isAcneProne}, Sensitive: ${skinProfile.isSensitive}, Hyperpigmentation: ${skinProfile.hasHyperpigmentation}, Climate: ${skinProfile.climateZone}."
                val fullSystemInstruction = "$systemInstruction\n$profileContext"

                val request = GeminiRequest(
                    contents = contents,
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.5f,
                        maxOutputTokens = 1024
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = fullSystemInstruction))
                    )
                )

                val response = try {
                    RetrofitClient.geminiService.generateContent("gemini-3.8-flash", apiKey, request)
                } catch (_: Exception) {
                    RetrofitClient.geminiService.generateContent("gemini-3.5-flash", apiKey, request)
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!replyText.isNullOrBlank()) {
                    val aiMsg = ChatMessage(sender = "model", text = replyText)
                    conversationHistory.add(aiMsg)
                    return@withContext aiMsg
                }
            } catch (e: Exception) {
                // Fall through to offline knowledge responder
            }
        }

        // Offline clinical rule-based assistant
        val fallbackReply = generateOfflineClinicalAdvice(userMessage, skinProfile)
        val aiMsg = ChatMessage(sender = "model", text = fallbackReply)
        conversationHistory.add(aiMsg)
        return@withContext aiMsg
    }

    private fun generateOfflineClinicalAdvice(query: String, profile: SkinProfile): String {
        val q = query.lowercase()
        return when {
            q.contains("bleach") || q.contains("lighten") || q.contains("hydroquinone") || q.contains("steroid") ->
                """
                **Clinical Toxicologist Warning:**
                • **Banned Ingredients**: Many illicit skin lighteners in Nigerian markets contain Hydroquinone (OTC ban) or Clobetasol Propionate (potent steroid).
                • **Ochronosis Hazard**: Prolonged hydroquinone use on melanin-rich skin triggers irreversible blue-black pigmentation (exogenous ochronosis) and skin thinning.
                • **Safe Alternatives**: Look for NAFDAC-registered products with **Niacinamide (2-5%)**, **Alpha Arbutin (1-2%)**, **Azelaic Acid (10%)**, or **Vitamin C**. These safely regulate melanosome transfer without destroying skin barrier cells.
                """.trimIndent()

            q.contains("acne") || q.contains("pimple") || q.contains("breakout") ->
                """
                **Dermatology Regimen for Acne in Tropical Weather:**
                • **Active Cleanser**: Use a gentle Salicylic Acid (BHA 1-2%) wash to dissolve sebum inside pores.
                • **Avoid Comedogenic Esters**: Check ingredient lists for Isopropyl Myristate and heavy coconut oils which clog pores in high humidity.
                • **Hydration**: Switch to water-based hyaluronic acid or lightweight gel moisturizers.
                • **Sunscreen**: Daily non-nano, invisible SPF 50 is critical to prevent dark spots from lingering.
                """.trimIndent()

            q.contains("sunscreen") || q.contains("spf") || q.contains("sun") ->
                """
                **Sun Protection for Melanin-Rich Skin:**
                • **Why It Matters**: UV rays trigger hyperactive melanocytes, causing post-acne dark marks to deepen and last for months.
                • **No White Cast**: Opt for chemical UV filters or micronized hybrid sunscreens that dry completely clear on dark skin.
                • **Reapplication**: In humid Nigerian heat, reapply every 2 hours if outdoors, especially in traffic or open markets.
                """.trimIndent()

            q.contains("counterfeit") || q.contains("fake") || q.contains("verify") || q.contains("market") ->
                """
                **Klarity Counterfeit Defense Checklist:**
                1. **Check NAFDAC Registration**: Look for the official registration number (e.g. A2-XXXX or 02-XXXX) and scan it in Klarity.
                2. **Inspect the Batch Stamp**: Authentic brands use dot-matrix inkjet or laser stamps with specific date/batch syntax, never blurry smeared prints.
                3. **Velocity Audit**: If buying in major wholesale hubs (Idumota, Balogun, Kantin Kwari), run a Klarity batch scan to ensure the batch code isn't a duplicated clone.
                4. **Texture & Scent**: Any separation of oil, chalky sediment, or overpowering chemical scent indicates counterfeit formulation.
                """.trimIndent()

            else ->
                """
                **Klarity Clinical Recommendation for ${profile.skinType.displayName}:**
                • Your profile is set for **${profile.climateZone}**.
                • For optimal skin barrier health, focus on gentle non-stripping cleansing, antioxidant protection (Vitamin C/Niacinamide), and adequate lightweight hydration.
                • Always verify packaging authenticity with Klarity before applying newly purchased products!
                """.trimIndent()
        }
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()
}
