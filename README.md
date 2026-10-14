# Klarity

**Anti-Counterfeit FMCG Authenticity Defense & Clinical Ingredient Intelligence**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20Clean-orange.svg)]()
[![Market](https://img.shields.io/badge/Market-Nigeria%20%26%20Emerging%20Markets-emerald.svg)]()

---

## 🌟 Overview

**Klarity** is a native Android application designed to protect consumers in Nigeria and emerging retail markets from counterfeit Fast-Moving Consumer Goods (FMCGs), launching with Phase 1 focused on **Cosmetics and Personal Care** (both international brands like Nivea, CeraVe, Neutrogena, Fair & White, and local Nigerian brands like Shea Origin, Arami Essentials, and Dudu-Osun).

Klarity addresses two critical consumer challenges:
1. **Authenticity Validation**: Real-time cross-referencing of GTIN barcodes, regulatory registration numbers, brand-specific batch syntax, and regional velocity anomalies to identify counterfeit packaging and cloned batch stamps across major retail hubs (Lagos, Kano, Abuja, Onitsha, Ibadan, Port Harcourt).
2. **Ingredient Intelligence & Toxicology**: Instant parsing of cosmetic ingredient lists (INCI) using Computer Vision and AI to provide clinical safety ratings, warn against hazardous bleaching agents (Hydroquinone, Clobetasol Propionate, Mercury), and evaluate formula suitability based on user skin profiles in tropical climates.

---

## 🛡️ Core Capabilities

### 1. 4-Layer Verification Engine
* **Layer 1: Central Ledger Existence** — Queries verified GTIN barcodes against registered brand formulas.
* **Layer 2: Regulatory Cross-Match** — Compares packaging registration numbers with authentic manufacturer filings.
* **Layer 3: Brand Batch Syntax Validation** — Evaluates stamped batch codes against manufacturer-specific regular expressions (e.g., CeraVe 6-character alphanumeric `^[0-9]{2}[A-Z][0-9]{3}$`, Nivea 8-digit numeric `^[0-9]{8}$`, Shea Origin `^SO-[A-Z]{2}-[0-9]{4}$`).
* **Layer 4: Cloned Batch Velocity Trap** — Detects duplicate batch codes scanned concurrently across distant geographical locations (e.g., Lagos vs. Kano), identifying mass-printed counterfeit packaging.

### 2. Clinical Ingredient Intelligence
* **Toxic Bleach & Steroid Screening** — Automatically flags banned and hazardous additives:
  * Hydroquinone (>2% without prescription)
  * Clobetasol Propionate (potent topical corticosteroid)
  * Mercury & ammoniated mercury salts
  * Monobenzone (permanent depigmentation hazard)
* **Comedogenic Scoring (0–5)** — Identifies pore-clogging esters and oils for acne-prone skin.
* **Tropical Climate Calibration** — Assesses heavy occlusives in high-humidity (Southern Nigeria) versus dry harmattan (Northern Nigeria) conditions.

### 3. Live CameraX Packaging Scanner
* **Hardware Camera Preview** — Real-time camera viewfinder powered by Android CameraX (`PreviewView`).
* **Persistent Permissions** — Camera permission is remembered once granted; no repetitive prompts.
* **Torch & Flashlight Control** — Instant illumination toggle for dimly lit retail markets.
* **Zero-Permission Photo Picker** — Android Photo Picker integration allowing users to select packaging photos directly from their gallery.

### 4. Flexible Nigerian State & Landmark Selector
* **All 36 States + FCT Abuja** — Consumers in any Nigerian state can select their location.
* **Custom Landmark Input** — Enter specific markets, plazas, malls, or local chemists (e.g., *Ikeja City Mall, Balogun Market, Trade Fair Complex, Wuse 2, Spar, Dugbe*).
* **State Presets** — Quick-tap suggestion chips for prominent commercial hubs.

### 5. DermAI Advisory Assistant
* **Clinical Dermatological Consultant** — Multi-turn chat assistant powered by Gemini AI (`gemini-3.8-flash`).
* **Melanin Skin Health** — Advises on hyperpigmentation/PIH, acne balancing, sun protection without white cast, and safe active ingredient routines.
* **Quick-Prompt Topics** — Single-tap chips for common consumer questions.

### 6. Design System & Theme
* **Palette**: Royal Gold (`#D4AF37`), Deep Sapphire Blue (`#2563EB`), and Crisp Clean White (`#FFFFFF`).
* **Minimalist Glassmorphism**: Translucent M3 cards with subtle borders, generous margins, and comfortable reading typography (14sp–16sp).
* **Theme Modes**: Seamless toggling between **System Default**, **Light Mode** (Crisp White & Royal Blue), and **Dark Mode** (Midnight Navy Obsidian & Radiant Gold).

---

## 🏗️ Architecture & Technology Stack

```
                          ┌──────────────────────────────────────┐
                          │         Android Application          │
                          │   Jetpack Compose (Material 3)       │
                          │   CameraX Live Viewfinder            │
                          │   StateFlow & MVVM Architecture      │
                          └──────────────────┬───────────────────┘
                                             │
                       ┌─────────────────────┴─────────────────────┐
                       ▼                                           ▼
            ┌──────────────────────┐                    ┌──────────────────────┐
            │   Room Database      │                    │     Gemini AI        │
            │   - Authentic Ledger │                    │   - Gemini 3.8 Flash │
            │   - User Skin Profile│                    │   - Toxicologist Bot │
            │   - Scan Audit Logs  │                    │   - INCI Extraction  │
            └──────────────────────┘                    └──────────────────────┘
```

* **Runtime**: Android 7.0+ (API 24 to 36)
* **Language**: 100% Kotlin
* **UI Toolkit**: Jetpack Compose with Material Design 3
* **Camera**: AndroidX CameraX (`camera2`, `camera-lifecycle`, `camera-view`)
* **Local Persistence**: AndroidX Room with Coroutine Flows
* **Network & REST API**: Retrofit 2 + Moshi + OkHttp Logging Interceptor
* **Image Loading**: Coil Compose
* **Asynchronous Concurrency**: Kotlin Coroutines & StateFlow

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Main Activity & Navigation Scaffold
├── model/
│   └── Models.kt                        # Core data models (Product, SkinProfile, Reports)
├── data/
│   ├── api/
│   │   ├── GeminiApiModels.kt           # Moshi request/response models for Gemini
│   │   └── GeminiApiService.kt          # Retrofit interface & OkHttp client
│   ├── local/
│   │   ├── Entities.kt                  # Room database entities
│   │   ├── Daos.kt                      # Room Data Access Objects
│   │   └── KlarityDatabase.kt           # Room Database & Authentic FMCG Seeds
│   └── repository/
│       ├── VerificationEngine.kt        # 4-Layer Authenticity Validation Engine
│       ├── IngredientIntelligenceEngine.kt # Clinical Toxicology & INCI Analyzer
│       └── DermAIChatRepository.kt      # DermAI Multi-Turn Gemini Integration
└── ui/
    ├── KlarityViewModel.kt              # App State & Business Logic
    ├── components/
    │   ├── CameraViewfinder.kt          # CameraX PreviewView Composable
    │   ├── CameraPermissionHandler.kt   # Lifecycle-aware camera permission logic
    │   ├── NigerianLocationSelector.kt  # 36 States + FCT & Landmark selector
    │   └── Components.kt                # Badges, Step Cards & Ingredient Cards
    ├── screens/
    │   ├── DashboardScreen.kt           # Ledger, Hero Scan CTA, Market Advisory
    │   ├── ScannerScreen.kt             # Viewfinder, Photo Picker & Form Inputs
    │   ├── VerificationResultScreen.kt  # Authenticity Breakdown & INCI Suite
    │   ├── DermAIChatScreen.kt          # Consultant Dermatology Chat
    │   ├── SkinProfileQuizScreen.kt     # Personal Skin Profiling Questionnaire
    │   ├── CommunityReportScreen.kt     # Counterfeit Vendor Community Alert
    │   └── SettingsAboutScreen.kt       # Theme Picker, Brand & Developer Bio
    └── theme/
        ├── Color.kt                     # Gold, Sapphire Blue & White Palette
        ├── Theme.kt                     # M3 Dark and Light Color Schemes
        └── Type.kt                      # Readable Typography Hierarchy
```

---

## 👨‍💻 Developer & Credits

**Akinremi Oluwatimileyin Temple**  
*Backend Software Engineer & AI Systems Architect*  
*Lagos, Nigeria*

* **GitHub**: [@thetemple10](https://github.com/thetemple10)
* **Academic Background**: B.Sc. Biochemistry, Obafemi Awolowo University (OAU), Ile-Ife, Nigeria.
* **Expertise**: Blending deep scientific training in biochemistry, pharmacology, and cosmetic toxicology with distributed backend architecture (Python/FastAPI, TypeScript, PostgreSQL, Redis, Docker) and native mobile engineering (Kotlin, Jetpack Compose) to build impactful consumer safety systems.

---

## ⚖️ Regulatory Disclaimer

*Klarity is an independent anti-counterfeit verification and cosmetic intelligence platform created to assist consumers and retailers. It is not directly operated by or legally affiliated with the National Agency for Food and Drug Administration and Control (NAFDAC) or any other government ministry. Authenticity assessments are based on registered public records, manufacturer specifications, and community velocity reporting.*

---

## 📄 License

This project is licensed under the MIT License — see the LICENSE file for details.
