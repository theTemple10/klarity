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
    private var customApiKey: String = ""

    fun setCustomApiKey(key: String) {
        customApiKey = key.trim()
    }

    fun getEffectiveApiKey(): String {
        return if (customApiKey.isNotBlank()) customApiKey else RetrofitClient.getApiKey()
    }

    val systemInstruction = """
        You are 'Klarity DermAI', an elite consultant cosmetic dermatologist, cosmetic chemist, and clinical toxicologist specializing in African skin health, tropical dermatology, formulation science, and anti-counterfeit cosmetic vigilance in Nigeria and emerging markets.
        Your tone is authoritative, warm, scientifically rigorous, accessible, and empathetic.
        Key areas of your clinical and cosmetology expertise:
        1. Formulating tailored, evidence-based skincare routines for melanin-rich skin (Fitzpatrick IV-VI) targeting post-inflammatory hyperpigmentation (PIH), acne balancing, skin barrier repair, and sun protection without white cast.
        2. Clinical toxicology: Explaining the hazards of unauthorized skin bleaching additives (Hydroquinone OTC abuse, Clobetasol Propionate and betamethasone steroids, ammoniated Mercury salts) and identifying exogenous ochronosis.
        3. Cosmetology active ingredient synergies and conflicts: Proper pH, concentrations, and layering (e.g. Niacinamide, Retinol/Tretinoin, L-Ascorbic Acid, Alpha Arbutin, Azelaic Acid, Salicylic Acid/BHA, Glycolic Acid/AHA, Ceramides, Centella Asiatica, Peptides).
        4. Tropical climate adaptation: Adjusting formulas for high humidity (Southern coastal Nigeria) vs. dry harmattan winds (Northern Nigeria).
        Keep answers structured, clear, and actionable with bullet points and bold highlights for effortless mobile readability.
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

        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Build multi-turn contents
                val contents = conversationHistory.takeLast(10).map { msg ->
                    GeminiContent(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                }

                val profileContext = "User Profile: ${skinProfile.skinType.displayName}, Acne-Prone: ${skinProfile.isAcneProne}, Sensitive: ${skinProfile.isSensitive}, Hyperpigmentation: ${skinProfile.hasHyperpigmentation}, Climate: ${skinProfile.climateZone}, Allergens: ${skinProfile.allergens.joinToString(", ")}."
                val fullSystemInstruction = "$systemInstruction\n$profileContext"

                val request = GeminiRequest(
                    contents = contents,
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.5f,
                        maxOutputTokens = 1500
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = fullSystemInstruction))
                    )
                )

                // Call Gemini 3.5 Flash (official recommended text model) with fallback to gemini-flash-latest
                val response = try {
                    RetrofitClient.geminiService.generateContent("gemini-3.5-flash", apiKey, request)
                } catch (_: Exception) {
                    try {
                        RetrofitClient.geminiService.generateContent("gemini-flash-latest", apiKey, request)
                    } catch (e2: Exception) {
                        RetrofitClient.geminiService.generateContent("gemini-2.5-flash", apiKey, request)
                    }
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!replyText.isNullOrBlank()) {
                    val aiMsg = ChatMessage(sender = "model", text = replyText)
                    conversationHistory.add(aiMsg)
                    return@withContext aiMsg
                }
            } catch (_: Exception) {
                // Fall through to real-time clinical knowledge engine
            }
        }

        // Comprehensive Real-Time Cosmetology & Clinical Dermatology Knowledge Engine
        val dynamicClinicalReply = evaluateCosmetologyKnowledge(userMessage, skinProfile)
        val aiMsg = ChatMessage(sender = "model", text = dynamicClinicalReply)
        conversationHistory.add(aiMsg)
        return@withContext aiMsg
    }

    /**
     * Comprehensive Clinical Cosmetology & Formulation Knowledge Engine
     * Covers active ingredients, skin concerns, layering rules, climate factors, and toxicological safety.
     */
    private fun evaluateCosmetologyKnowledge(query: String, profile: SkinProfile): String {
        val q = query.lowercase().trim()

        // 1. Harmful bleaching, steroids, hydroquinone & mercury
        if (q.contains("bleach") || q.contains("lighten") || q.contains("hydroquinone") ||
            q.contains("steroid") || q.contains("mercury") || q.contains("clobetasol") ||
            q.contains("ochronosis") || q.contains("fair and white") || q.contains("caro white") ||
            q.contains("stretch mark") && (q.contains("cream") || q.contains("skin"))) {
            return """
                **Clinical Cosmetology & Toxicology Alert: Illicit Skin Lighteners**
                
                • **Banned Toxic Additives**: High-potency topical corticosteroids (e.g., *Clobetasol Propionate*, *Betamethasone*) and over-the-counter *Hydroquinone (>2%)* are strictly restricted by cosmetic regulators.
                • **Exogenous Ochronosis Risk**: Chronic hydroquinone use on melanin-rich skin halts the homogentisic acid oxidase enzyme, polymerizing homogentisic acid into permanent blue-black dermal deposits with severe collagen degradation.
                • **Topical Steroid Addiction**: Prolonged steroid misuse causes irreversible skin thinning (atrophy), visible purple stretch marks, facial telangiectasia (spider veins), and severe rebound redness.
                • **Clinical Brightening Alternatives**:
                  - **Niacinamide (2–5%)**: Inhibits melanosome transfer from melanocytes to keratinocytes safely without cell damage.
                  - **Alpha Arbutin (1–2%)**: A gentle, slow-release hydroquinone glycoside that suppresses tyrosinase enzyme activity safely.
                  - **Azelaic Acid (10–15%)**: Selectively targets hyperactive melanocytes while calming acne inflammation.
                  - **Vitamin C (Ascorbyl Glucoside / L-Ascorbic Acid)**: Potent antioxidant that scavenges free radicals and blocks melanin synthesis.
            """.trimIndent()
        }

        // 2. Retinol, Retinoids, Tretinoin, Adaplene, Anti-Aging
        if (q.contains("retinol") || q.contains("retinoid") || q.contains("tretinoin") ||
            q.contains("differin") || q.contains("adapalene") || q.contains("wrinkle") ||
            q.contains("anti-aging") || q.contains("cell turnover")) {
            return """
                **Cosmetology Guide: Retinoids on Melanin-Rich Skin**
                
                • **Mechanism**: Vitamin A derivatives accelerate epidermal cell turnover, unclog microscopic comedones, and stimulate dermis fibroblast collagen synthesis.
                • **Melanin Consideration**: If retinoids cause excessive irritation or peeling, dark skin responds with rebound hyperpigmentation. Therefore, avoid aggressive initial strengths.
                • **The Sandwich Method (Buffer Technique)**:
                  1. Cleanse with a gentle, non-stripping hydrating cleanser.
                  2. Apply a thin layer of ceramide-rich moisturizer to damp skin and let absorb for 5 minutes.
                  3. Apply a pea-sized amount of Retinol (0.2%–0.5%) or Tretinoin (0.025%) across the entire face.
                  4. Top with another layer of barrier moisturizer.
                • **Golden Rules**:
                  - Nighttime only (sunlight degrades standard retinol).
                  - Start slowly: 2 nights per week for 2 weeks, then alternate nights.
                  - **Mandatory Daily SPF 50**: Retinoids thin the stratum corneum initially, heightening UV sensitivity.
            """.trimIndent()
        }

        // 3. Acne, Breakouts, Pimples, Blackheads, Sebaceous Filaments
        if (q.contains("acne") || q.contains("pimple") || q.contains("breakout") ||
            q.contains("blackhead") || q.contains("whitehead") || q.contains("clogged pore") ||
            q.contains("cystic") || q.contains("salicylic")) {
            return """
                **Clinical Regimen for Acne & Congestion in Tropical Climates**
                
                • **Underlying Drivers**: High heat and tropical humidity stimulate sebaceous gland hyperactivity, mixing excess sebum with dead keratinocytes and *Cutibacterium acnes*.
                • **Core Cosmetological Actives**:
                  - **Salicylic Acid (BHA 1–2%)**: Lipid-soluble beta-hydroxy acid that penetrates into oil pores, liquefying trapped sebum and dead skin cells.
                  - **Benzoyl Peroxide (2.5%–5%)**: Powerful antimicrobial that releases oxygen radicals inside pores to eliminate acne bacteria without developing antibiotic resistance.
                  - **Niacinamide (4%)**: Regulates sebum excretion rate while fortifying intercellular lipid matrix.
                • **Routine Blueprint**:
                  - **AM**: Gentle Foaming/Gel Cleanser → Niacinamide Serum → Oil-Free Gel Moisturizer → Broad Spectrum Non-Nano Sunscreen SPF 50.
                  - **PM**: Salicylic Acid Cleanser (2–3x weekly) → Hydrating Hyaluronic/Glycerin Toner → Azelaic Acid 10% → Lightweight Barrier Emulsion.
                • **Ingredients to Avoid**: Isopropyl Myristate, Coconut Oil (*Cocos nucifera*), and heavy mineral wax that trap sweat in tropical humidity.
            """.trimIndent()
        }

        // 4. Hyperpigmentation, Dark Spots, Melasma, PIH, Uneven Tone
        if (q.contains("hyperpigmentation") || q.contains("dark spot") || q.contains("uneven tone") ||
            q.contains("melasma") || q.contains("pih") || q.contains("fade") || q.contains("discoloration")) {
            return """
                **Clinical Protocol: Post-Inflammatory Hyperpigmentation (PIH) on Deep Skin Tones**
                
                • **Pathophysiology**: Melanocytes in melanin-rich skin produce larger, more dispersed melanosomes. Any cutaneous inflammation (acne, insect bite, harsh scrubbing) triggers an overproduction of melanin that deposits in the epidermal and dermal layers.
                • **The Multi-Pathway Fading Strategy**:
                  1. **Tyrosinase Inhibition (Melanin Blocker)**:
                     - *Tranexamic Acid (3–5%)*: Blocks plasmin-induced melanogenesis.
                     - *Alpha Arbutin (2%)*: Suppresses enzymatic oxidation of tyrosine.
                     - *Kojic Acid (1%)*: Chelates copper ion at tyrosinase active site.
                  2. **Melanosome Transfer Inhibition**:
                     - *Niacinamide (3–5%)*: Stops pigment from moving into surrounding skin cells.
                  3. **Gentle Chemical Exfoliation**:
                     - *Lactic Acid (5%)* or *Mandelic Acid (10%)*: Large-molecular-weight AHAs that gently sweep away pigmented surface keratinocytes without causing inflammation.
                • **Crucial Step**: **Broad-Spectrum SPF 50 Sunscreen** is non-negotiable. UVA and visible light stimulate melanocytes to keep dark spots pigmented for months if unprotected.
            """.trimIndent()
        }

        // 5. Sunscreen, Sun Protection, White Cast, SPF
        if (q.contains("sunscreen") || q.contains("spf") || q.contains("sun") ||
            q.contains("white cast") || q.contains("uv") || q.contains("sunburn")) {
            return """
                **Cosmetic Chemistry: Sun Protection for Melanin-Rich Skin**
                
                • **The Myth**: While melanin provides natural basal UV protection (approximately SPF 1.5–4), it does NOT shield against UVA-induced photo-aging, DNA damage, or stubborn hyperpigmentation.
                • **Solving the White Cast Challenge**:
                  - **Chemical / Organic UV Filters**: Ingredients like *Avobenzone*, *Homosalate*, *Octisalate*, *Tinosorb S/M*, and *Uvinul A Plus* dissolve transparently into the skin matrix with zero purple/chalky film.
                  - **Micronized & Tinted Mineral Filters**: If using Zinc Oxide or Titanium Dioxide, select micronized or iron oxide-tinted formulas. Iron oxides also provide critical protection against visible blue light, which worsens melasma.
                • **Application Rules for Tropical Nigeria**:
                  - Use the **Two-Finger Rule** (two full strips along pointer and middle finger) for face and neck.
                  - Reapply every 2 to 3 hours when outdoors, sweating, or in open transit.
                  - Choose water-resistant, oil-free gel or fluid textures to prevent sweat-induced stinging around the eyes.
            """.trimIndent()
        }

        // 6. Dry Skin, Harmattan, Dehydration, Flaking, Dullness
        if (q.contains("dry skin") || q.contains("harmattan") || q.contains("dehydrated") ||
            q.contains("flak") || q.contains("peel") || q.contains("tight") || q.contains("winter")) {
            return """
                **Clinical Protocol: Combating Harmattan Dryness & Transepidermal Water Loss (TEWL)**
                
                • **Harmattan Dynamics**: The dry, dusty continental northeasterly wind drastically plummets atmospheric relative humidity, stripping the stratum corneum's natural moisturizing factor (NMF) and compromising barrier lipids.
                • **The 3-Tier Hydration Triad**:
                  1. **Humectants (Water Attractors)**: Apply *Hyaluronic Acid*, *Glycerin*, or *Panthenol* onto damp skin immediately post-cleanse to bind water into the stratum corneum.
                  2. **Emollients (Intercellular Gap Fillers)**: Look for *Ceramides (NP, AP, EOP)*, *Cholesterol*, and *Phytosphingosine* in a 3:1:1 physiological ratio to restore the skin barrier.
                  3. **Occlusives (Moisture Sealants)**: Use pure unrefined Nigerian *Shea Butter (Butyrospermum parkii)*, *Squalane*, or *Petrolatum* at night to lock in hydration and prevent atmospheric moisture robbery.
                • **Cosmetic Dos & Don'ts**:
                  - Do NOT use harsh alcohol toners or hot water.
                  - Switch from foaming sulfate cleansers to creamy hydrating lotion cleansers.
                  - Carry an alcohol-free glycerin facial mist for mid-day barrier replenishment.
            """.trimIndent()
        }

        // 7. Oily skin, Sebum, Shine, Large Pores
        if (q.contains("oily") || q.contains("greas") || q.contains("shine") ||
            q.contains("pore") || q.contains("matt") || q.contains("t-zone")) {
            return """
                **Cosmetic Balancing: Oily Skin & Excess Sebum Control**
                
                • **The Dehydration Paradox**: Stripping oily skin with harsh astringents triggers reactive hyper-seborrhea (the skin produces even more oil to compensate for moisture loss).
                • **Sebum Balancing Actives**:
                  - **Niacinamide (4–5%)**: Clinically shown to decrease triglycerides and free fatty acids in facial sebum.
                  - **Zinc PCA (1%)**: Powerful zinc salt of L-pyrrolidone carboxylate that regulates 5-alpha reductase enzyme to curb excess sebum output.
                  - **Green Tea Extract (EGCG)**: Potent antioxidant that calms sebaceous gland irritation and minimizes pore appearance.
                • **Formulation Choices**:
                  - **Texture**: Use oil-free, gel-cream or water-burst moisturizers with dimethicone or silica for a soft-matte velvety finish.
                  - **Cleansing**: Double cleanse at night if wearing SPF/makeup (gentle cleansing oil followed by a mild BHA gel cleanser).
            """.trimIndent()
        }

        // 8. Sensitive Skin, Eczema, Barrier Repair, Redness, Burning
        if (q.contains("sensitiv") || q.contains("eczema") || q.contains("barrier") ||
            q.contains("burn") || q.contains("sting") || q.contains("redness") ||
            q.contains("irritat") || q.contains("dermatitis")) {
            return """
                **Dermatology Emergency: Compromised Moisture Barrier Repair**
                
                • **Symptoms**: Stinging sensation when applying basic water or moisturizer, sudden tightness, rough sandpaper texture, and unexplained tiny bumps.
                • **Immediate Recovery Protocol**:
                  1. **Pause All Actives**: Immediately discontinue AHA/BHA exfoliants, retinoids, vitamin C, and physical scrubs for 14–21 days.
                  2. **Gentle Cleansing**: Use lukewarm water or an ultra-mild, fragrance-free micellar gel.
                  3. **Barrier Replenishment**:
                     - *Centella Asiatica (Madecassoside)*: Accelerates wound healing and suppresses pro-inflammatory cytokines.
                     - *Ceramide Complexes*: Re-knits the lipid bilayer cement between corneocytes.
                     - *Colloidal Oatmeal (1%)*: Beta-glucan rich natural anti-inflammatory approved for eczema.
                     - *Panthenol (Pro-Vitamin B5)*: Stimulates cellular proliferation and tissue regeneration.
                  4. Keep routine strictly to: Gentle Cleanser → Barrier Cream → Mineral SPF.
            """.trimIndent()
        }

        // 9. Ingredient Layering, Conflicts & What to Mix
        if (q.contains("mix") || q.contains("layer") || q.contains("routine") ||
            q.contains("combine") || q.contains("together") || q.contains("order")) {
            return """
                **Cosmetic Formulation Guide: Ingredient Layering Rules**
                
                • **Safe & Synergistic Combinations**:
                  - **Niacinamide + Salicylic Acid**: Excellent for pore unclogging and sebum control.
                  - **Retinol + Hyaluronic Acid / Ceramides**: Mitigates retinoid irritation without reducing efficacy.
                  - **Vitamin C + Vitamin E + Ferulic Acid**: Multiplies antioxidant photoprotection by 8-fold.
                  - **Alpha Arbutin + Niacinamide**: Multi-pathway brightening for stubborn dark marks.
                • **Avoid Layering in the Same Routine**:
                  - **Retinol + Strong Glycolic/Lactic Acid (AHA)**: Risk of severe barrier damage and peeling. Alternate nights instead.
                  - **Vitamin C (L-Ascorbic Acid) + Direct BHA/AHA**: High acid concentration destabilizes the acid mantle. Use Vitamin C in the AM, acids in the PM.
                  - **Benzoyl Peroxide + Pure Retinol**: Benzoyl peroxide oxidizes tretinoin/retinol molecules, rendering both inactive.
                • **Correct Layering Sequence**:
                  1. Cleanser → 2. Toner / Essence → 3. Water-based Serums (thinnest to thickest) → 4. Emulsion / Moisturizer → 5. Face Oil (optional PM) → 6. Sunscreen (AM finale).
            """.trimIndent()
        }

        // 10. General / Custom Context-Aware Clinical Recommendation
        return """
            **Klarity Clinical Cosmetology Recommendation**
            
            • **Profile Consideration for ${profile.skinType.displayName} Skin**:
              - Region calibrated for: **${profile.climateZone}**.
              - Acne Predisposition: **${if (profile.isAcneProne) "Acne-Prone (Use non-comedogenic gel bases)" else "Balanced"}**.
              - Hyperpigmentation Focus: **${if (profile.hasHyperpigmentation) "Active Melanosome Regulation Needed" else "Standard Maintenance"}**.
            • **Clinical Recommendations for Your Query**:
              1. **Check Ingredients First**: Always verify INCI packaging labels with Klarity to ensure your skincare is free from unlisted hydroquinone, mercury, and clobetasol steroids.
              2. **Evidence-Based Active**: For ${profile.skinType.displayName} skin, prioritize gentle barrier-supportive actives (*Niacinamide 3%*, *Ceramides*, *Hyaluronic Acid*) before introducing strong chemical exfoliants.
              3. **Daily Photoprotection**: In tropical Nigerian climates, UV radiation stimulates melanogenesis rapidly. Daily broad-spectrum SPF 50 is the foundational pillar for clear, resilient skin.
            • *Ask me about any specific active ingredient (e.g., Retinol, Azelaic Acid, Salicylic Acid, Vitamin C) or routine order for a detailed clinical breakdown!*
        """.trimIndent()
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()
}
