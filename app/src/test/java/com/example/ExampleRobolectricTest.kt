package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.IngredientIntelligenceEngine
import com.example.model.IngredientAction
import com.example.model.SkinProfile
import com.example.model.SkinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Klarity", appName)
    }

    @Test
    fun `ingredient intelligence detects toxic bleaching agents`() {
        val engine = IngredientIntelligenceEngine()
        val toxicIngredients = listOf("Aqua", "Hydroquinone", "Clobetasol Propionate", "Mercury")
        val profile = SkinProfile(skinType = SkinType.OILY)
        val analysis = engine.evaluateLocalDermatologyEngine(toxicIngredients, profile)

        assertEquals("DANGEROUS", analysis.overallScore)
        assertTrue(analysis.toxicWarnings.isNotEmpty())
    }

    @Test
    fun `ingredient intelligence evaluates beneficial actives for melanin skin`() {
        val engine = IngredientIntelligenceEngine()
        val healthyIngredients = listOf("Aqua", "Glycerin", "Niacinamide", "Ceramide NP", "Hyaluronic Acid")
        val profile = SkinProfile(skinType = SkinType.OILY, hasHyperpigmentation = true)
        val analysis = engine.evaluateLocalDermatologyEngine(healthyIngredients, profile)

        assertEquals("EXCELLENT", analysis.overallScore)
        assertTrue(analysis.ingredients.any { it.name.contains("Niacinamide") && it.action == IngredientAction.PRO })
    }
}
