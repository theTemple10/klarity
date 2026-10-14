package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Klarity Gold, Blue & White Palette (and coordinated derivatives)
// =========================================================================

// --- Gold Shades ---
val KlarityGoldPrimary = Color(0xFFE5A93C)   // Warm, luminous royal gold
val KlarityGoldLight = Color(0xFFFFD166)     // Vibrant bright gold highlight
val KlarityGoldDark = Color(0xFFC58E1B)      // Deep burnished antique gold
val KlarityGoldGlow = Color(0xFFFFF3D6)      // Creamy champagne gold tint
val KlarityGoldBorder = Color(0x66E5A93C)    // Translucent gold border

// --- Blue Shades ---
val RoyalBlue = Color(0xFF1E40AF)           // Classic deep royal blue
val SapphireBlue = Color(0xFF2563EB)        // Vivid electric sapphire blue
val SkyBlueAccent = Color(0xFF60A5FA)       // Cheerful energetic blue
val NavyDeep = Color(0xFF0A1128)            // Midnight navy background (Dark Mode)
val NavySurface = Color(0xFF101F42)         // Rich navy surface (Dark Mode)
val NavySurfaceVariant = Color(0xFF182D5C)  // Navy card container (Dark Mode)
val NavyBorder = Color(0xFF27437D)          // Subtle slate-blue border
val SoftBlueLight = Color(0xFFF0F5FF)       // Light theme airy blue tint
val IceBlueTint = Color(0xFFE0ECFF)         // Light theme card surface variant

// --- White & Neutrals ---
val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF8FAFC)            // Crisp clean white (Light Mode scaffold)
val SlateLight = Color(0xFF94A3B8)          // Muted slate text
val SlateDark = Color(0xFF475569)           // Secondary text for Light Mode
val MidnightBlue = Color(0xFF0A1931)        // Deep navy text for Light Mode
val SlateNavySubtle = Color(0xFFE2E8F0)     // Divider/border for Light Mode

// --- System Status Colors (harmonized with Gold & Blue) ---
val StatusPassGreen = Color(0xFF10B981)
val StatusPassGreenBg = Color(0x2610B981)
val StatusWarningGold = Color(0xFFF59E0B)
val StatusWarningGoldBg = Color(0x26F59E0B)
val StatusAlertRed = Color(0xFFEF4444)
val StatusAlertRedBg = Color(0x26EF4444)

// Legacy / compatibility aliases mapped to new palette
val ElectricCyan = KlarityGoldLight
val TealDark = SapphireBlue
val LightSlate = SlateLight
val ObsidianBase = NavyDeep
val SlateSurface = NavySurface
val SlateSurfaceVariant = NavySurfaceVariant
val SlateContainer = NavySurfaceVariant
val SlateBorder = NavyBorder
val StatusWarningYellow = StatusWarningGold
val StatusWarningYellowBg = StatusWarningGoldBg
val ToxicPurple = Color(0xFF8B5CF6)
val AmberShield = KlarityGoldPrimary
val ClinicalBlue = SapphireBlue
