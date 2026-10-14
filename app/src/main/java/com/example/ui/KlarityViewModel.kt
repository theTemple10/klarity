package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CounterfeitReportEntity
import com.example.data.local.KlarityDatabase
import com.example.data.local.ScanLogEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.DermAIChatRepository
import com.example.data.repository.IngredientIntelligenceEngine
import com.example.data.repository.VerificationEngine
import com.example.model.ChatMessage
import com.example.model.IngredientAnalysis
import com.example.model.SkinProfile
import com.example.model.SkinType
import com.example.model.VerificationReport
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class Screen {
    DASHBOARD,
    SCANNER,
    RESULT,
    DERM_AI,
    SKIN_QUIZ,
    REPORT_COUNTERFEIT,
    SETTINGS_ABOUT
}

class KlarityViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KlarityDatabase.getDatabase(application, viewModelScope)
    private val productDao = database.productDao()
    private val scanLogDao = database.scanLogDao()
    private val reportDao = database.reportDao()
    private val userProfileDao = database.userProfileDao()

    private val verificationEngine = VerificationEngine(productDao, scanLogDao)
    private val ingredientEngine = IngredientIntelligenceEngine()
    private val dermAiRepo = DermAIChatRepository()

    // App Theme Mode (System Default, Light, Dark)
    private val _appThemeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    // Navigation & Screen Stack
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Verification Report
    private val _verificationReport = MutableStateFlow<VerificationReport?>(null)
    val verificationReport: StateFlow<VerificationReport?> = _verificationReport.asStateFlow()

    // Ingredient Analysis
    private val _ingredientAnalysis = MutableStateFlow<IngredientAnalysis?>(null)
    val ingredientAnalysis: StateFlow<IngredientAnalysis?> = _ingredientAnalysis.asStateFlow()

    // Loading indicators
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // User Skin Profile
    private val _skinProfile = MutableStateFlow(
        SkinProfile(
            skinType = SkinType.OILY,
            isAcneProne = true,
            hasHyperpigmentation = true,
            isSensitive = false,
            climateZone = "Tropical Humid (Lagos / Southern Nigeria)",
            allergens = listOf("Artificial Fragrance", "Parabens")
        )
    )
    val skinProfile: StateFlow<SkinProfile> = _skinProfile.asStateFlow()

    // Recent Scans from Room
    val scanLogs: StateFlow<List<ScanLogEntity>> = scanLogDao.getAllScanLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "model",
                text = "Hello! I am Klarity DermAI, your consultant cosmetic dermatologist and clinical toxicologist. I can help evaluate cosmetic ingredients, screen for illicit bleaching agents, recommend acne & hyperpigmentation routines for melanin-rich skin, and guide you on authentic product verification across Nigerian markets. How can I assist you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        // Observe stored profile
        viewModelScope.launch {
            userProfileDao.getUserProfile().collect { stored ->
                if (stored != null) {
                    val type = try {
                        SkinType.valueOf(stored.skinType)
                    } catch (_: Exception) {
                        SkinType.OILY
                    }
                    _skinProfile.value = SkinProfile(
                        skinType = type,
                        isAcneProne = stored.isAcneProne,
                        hasHyperpigmentation = stored.hasHyperpigmentation,
                        isSensitive = stored.isSensitive,
                        climateZone = stored.climateZone,
                        allergens = stored.allergens.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    )
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun performVerification(
        barcode: String,
        scannedNafdac: String,
        batchCode: String,
        location: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val report = verificationEngine.verifyProduct(barcode, scannedNafdac, batchCode, location)
            _verificationReport.value = report

            // Also automatically trigger ingredient intelligence on the product's formula
            if (report.product != null && report.product.ingredients.isNotEmpty()) {
                val analysis = ingredientEngine.analyzeIngredients(report.product.ingredients, _skinProfile.value)
                _ingredientAnalysis.value = analysis
            } else {
                _ingredientAnalysis.value = null
            }

            _isLoading.value = false
            _currentScreen.value = Screen.RESULT
        }
    }

    fun analyzeCustomIngredients(ingredientsText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val list = ingredientsText.split(",", ";", "\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val analysis = ingredientEngine.analyzeIngredients(list, _skinProfile.value)
            _ingredientAnalysis.value = analysis

            // Create synthetic report for custom ingredient checks
            _verificationReport.value = VerificationReport(
                overallStatus = if (analysis.overallScore == "DANGEROUS") VerificationStatus.RED else VerificationStatus.YELLOW,
                statusHeadline = if (analysis.overallScore == "DANGEROUS") "Toxic Substances Detected" else "Ingredient Safety Analysis",
                statusExplanation = analysis.summary,
                steps = emptyList(),
                product = null,
                scannedBarcode = "Custom OCR Input",
                scannedBatchCode = "N/A",
                scannedNafdac = "N/A",
                scanLocation = "Direct Text Input",
                velocityCount = 1,
                velocityLocations = listOf("User Session")
            )

            _isLoading.value = false
            _currentScreen.value = Screen.RESULT
        }
    }

    fun sendDermAiMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true
            val reply = dermAiRepo.sendMessage(text, _skinProfile.value)
            _chatMessages.value = _chatMessages.value + reply
            _isChatLoading.value = false
        }
    }

    fun saveSkinProfile(newProfile: SkinProfile) {
        _skinProfile.value = newProfile
        viewModelScope.launch {
            userProfileDao.saveUserProfile(
                UserProfileEntity(
                    id = 1,
                    skinType = newProfile.skinType.name,
                    isAcneProne = newProfile.isAcneProne,
                    hasHyperpigmentation = newProfile.hasHyperpigmentation,
                    isSensitive = newProfile.isSensitive,
                    climateZone = newProfile.climateZone,
                    allergens = newProfile.allergens.joinToString(", ")
                )
            )
            // Re-evaluate current analysis if active
            _verificationReport.value?.product?.let { product ->
                _ingredientAnalysis.value = ingredientEngine.analyzeIngredients(product.ingredients, newProfile)
            }
        }
    }

    fun submitReport(
        productName: String,
        brandName: String,
        batchCode: String,
        marketName: String,
        city: String,
        sellerDetails: String
    ) {
        viewModelScope.launch {
            reportDao.insertReport(
                CounterfeitReportEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    productName = productName,
                    brandName = brandName,
                    batchCode = batchCode,
                    marketName = marketName,
                    city = city,
                    sellerDetails = sellerDetails,
                    timestamp = System.currentTimeMillis()
                )
            )
            // Return to dashboard after filing report
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    // Preset Scenarios demonstrating all 4 Authenticity Validation Flow paths
    fun runPreset(scenario: PresetScenario) {
        when (scenario) {
            PresetScenario.NIVEA_AUTHENTIC -> {
                performVerification(
                    barcode = "4005900940567",
                    scannedNafdac = "A2-4412",
                    batchCode = "34819022",
                    location = "Lagos (Ikeja City Mall)"
                )
            }
            PresetScenario.CERAVE_NAFDAC_TAMPERED -> {
                performVerification(
                    barcode = "3337875597203",
                    scannedNafdac = "99-FAKE-NAFDAC", // Tampered
                    batchCode = "20W301",
                    location = "Lagos (Balogun Market)"
                )
            }
            PresetScenario.CLONED_BATCH_TRAP -> {
                performVerification(
                    barcode = "4005900940567",
                    scannedNafdac = "A2-4412",
                    batchCode = "98214432", // Scanned earlier in Kano and Idumota
                    location = "Abuja (Wuse II)"
                )
            }
            PresetScenario.UNVERIFIED_COMMUNITY -> {
                performVerification(
                    barcode = "7981240091823", // Non-existent
                    scannedNafdac = "UNLISTED",
                    batchCode = "BATCH-001",
                    location = "Onitsha (Main Market)"
                )
            }
            PresetScenario.TOXIC_BLEACH_CREAM -> {
                analyzeCustomIngredients(
                    "Aqua, Paraffinum Liquidum, Hydroquinone (4%), Clobetasol Propionate (0.05%), Mercury, Fragrance, Stearic Acid"
                )
            }
            PresetScenario.SHEA_ORIGIN_AUTHENTIC -> {
                performVerification(
                    barcode = "6151101928014",
                    scannedNafdac = "A4-2281",
                    batchCode = "SO-LA-2024",
                    location = "Ibadan (Dugbe)"
                )
            }
        }
    }
}

enum class PresetScenario {
    NIVEA_AUTHENTIC,
    CERAVE_NAFDAC_TAMPERED,
    CLONED_BATCH_TRAP,
    UNVERIFIED_COMMUNITY,
    TOXIC_BLEACH_CREAM,
    SHEA_ORIGIN_AUTHENTIC
}
