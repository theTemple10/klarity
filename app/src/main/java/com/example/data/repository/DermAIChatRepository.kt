package com.example.data.repository

import android.util.Log
import com.example.data.ai.FirebaseGeminiGateway
import com.example.data.ai.GeminiGateway
import com.example.model.ChatMessage
import com.example.model.SkinProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DermAIChatRepository(
    private val geminiGateway: GeminiGateway = FirebaseGeminiGateway()
) {

    private val baseSystemInstruction = """
        You are DermAI, a skincare and cosmetic ingredient guide for people in Nigeria.
        Your job is to answer questions about cosmetics, product ingredients, routines, and skin safety.
        Keep answers clear, grounded, and concise: 3 to 5 short lines first.
        Use plain text formatting without unrendered markdown asterisks.
        Do not diagnose medical conditions or present yourself as a medical doctor.
        If the user asks about prescription steroids (clobetasol, betamethasone), mercury, hydroquinone abuse, or symptoms like spreading rashes, infections, or painful burns, advise them plainly to consult a licensed dermatologist or pharmacist.
    """.trimIndent()

    suspend fun sendMessage(
        userMessage: String,
        skinProfile: SkinProfile,
        recentHistory: List<ChatMessage> = emptyList(),
        productContext: String? = null
    ): ChatMessage = withContext(Dispatchers.IO) {
        val profileContext = "User skin profile: ${skinProfile.skinType.displayName}, acne-prone: ${skinProfile.isAcneProne}, sensitive: ${skinProfile.isSensitive}, hyperpigmentation: ${skinProfile.hasHyperpigmentation}, location/climate: ${skinProfile.climateZone}."
        val fullInstruction = buildString {
            append(baseSystemInstruction)
            append("\n\n")
            append(profileContext)
            if (!productContext.isNullOrBlank()) {
                append("\nContext on currently scanned product: ")
                append(productContext)
            }
        }

        val historyPairs = recentHistory.takeLast(6).map { msg ->
            (if (msg.sender == "user") "user" else "model") to msg.text
        }

        try {
            val result = geminiGateway.chat(
                history = historyPairs,
                newMessage = userMessage,
                systemInstruction = fullInstruction
            )

            if (result.isSuccess) {
                val replyText = result.getOrNull()
                if (!replyText.isNullOrBlank()) {
                    return@withContext ChatMessage(
                        sender = "model",
                        text = replyText.replace("**", "").replace("##", "").trim()
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("DermAIRepo", "Chat request failed: ${e.message}", e)
        }

        // Offline guidance fallback
        val offlineTip = findRelevantOfflineTip(userMessage)
        if (offlineTip != null) {
            ChatMessage(
                sender = "model",
                text = "Can't reach the assistant. Showing general tips:\n\n$offlineTip",
                isError = false
            )
        } else {
            ChatMessage(
                sender = "model",
                text = "Can't reach the assistant. Please check your network connection and try again.",
                isError = true
            )
        }
    }

    private fun findRelevantOfflineTip(query: String): String? {
        val q = query.lowercase().trim()
        return when {
            q.contains("bleach") || q.contains("lighten") || q.contains("hydroquinone") ||
                    q.contains("steroid") || q.contains("mercury") || q.contains("clobetasol") ->
                """
                - Avoid products with hydroquinone, clobetasol propionate or mercury salts.
                - For safe brightening, look for Niacinamide (2-5%), Alpha Arbutin (1-2%), or Azelaic Acid (10%).
                - If you notice skin thinning or blue-black discoloration, stop use and see a dermatologist.
                """.trimIndent()

            q.contains("retinol") || q.contains("tretinoin") || q.contains("retinoid") ->
                """
                - Start slowly: 2 nights a week on clean, dry skin.
                - Buffer with a gentle moisturizer if you experience peeling or sensitivity.
                - Use only at night and wear broad-spectrum sunscreen daily.
                """.trimIndent()

            q.contains("acne") || q.contains("pimple") || q.contains("breakout") ->
                """
                - Wash with a gentle salicylic acid (BHA) or benzoyl peroxide cleanser.
                - Avoid heavy coconut oil and isopropyl myristate in high humidity.
                - Use a lightweight, oil-free gel moisturizer to protect your skin barrier.
                """.trimIndent()

            q.contains("sunscreen") || q.contains("spf") || q.contains("sun") || q.contains("white cast") ->
                """
                - Sunscreen helps prevent dark spots and hyperpigmentation from deepening.
                - Chemical filters or tinted mineral sunscreens blend clearly without white cast on dark skin.
                - Apply two finger lengths to your face and neck every morning.
                """.trimIndent()

            q.contains("dry") || q.contains("harmattan") || q.contains("flaky") ->
                """
                - Apply hyaluronic acid or glycerin while your skin is still damp after washing.
                - Lock in moisture with pure unrefined shea butter or a ceramide barrier cream.
                - Avoid hot water and harsh alcohol-based astringents.
                """.trimIndent()

            q.contains("dark spot") || q.contains("hyperpigmentation") || q.contains("pih") || q.contains("fade") ->
                """
                - Post-inflammatory hyperpigmentation fades with consistent gentle actives.
                - Consider Tranexamic Acid, Alpha Arbutin, or Azelaic Acid.
                - Daily SPF is essential to keep UV light from triggering excess melanin.
                """.trimIndent()

            else -> null
        }
    }
}
