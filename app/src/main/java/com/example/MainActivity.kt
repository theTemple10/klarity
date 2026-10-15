package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppThemeMode
import com.example.ui.KlarityViewModel
import com.example.ui.Screen
import com.example.ui.screens.CommunityReportScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DermAIChatScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsAboutScreen
import com.example.ui.screens.SkinProfileQuizScreen
import com.example.ui.screens.VerificationResultScreen
import com.example.ui.theme.KlarityGoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SapphireBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KlarityRoot()
        }
    }
}

@Composable
fun KlarityRoot(viewModel: KlarityViewModel = viewModel()) {
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (appThemeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        KlarityApp(viewModel = viewModel)
    }
}

@Composable
fun KlarityApp(viewModel: KlarityViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val skinProfile by viewModel.skinProfile.collectAsState()
    val recentScans by viewModel.scanLogs.collectAsState()
    val verificationReport by viewModel.verificationReport.collectAsState()
    val ingredientAnalysis by viewModel.ingredientAnalysis.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()

    val showBottomBar = currentScreen == Screen.DASHBOARD ||
            currentScreen == Screen.SCANNER ||
            currentScreen == Screen.DERM_AI ||
            currentScreen == Screen.SKIN_QUIZ ||
            currentScreen == Screen.SETTINGS_ABOUT

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = KlarityGoldPrimary,
                    modifier = Modifier.testTag("klarity_bottom_navigation")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Dashboard") },
                        label = { Text("Ledger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KlarityGoldPrimary,
                            selectedTextColor = KlarityGoldPrimary,
                            indicatorColor = KlarityGoldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SCANNER,
                        onClick = { viewModel.navigateTo(Screen.SCANNER) },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan") },
                        label = { Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KlarityGoldPrimary,
                            selectedTextColor = KlarityGoldPrimary,
                            indicatorColor = KlarityGoldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_scanner")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.DERM_AI,
                        onClick = { viewModel.navigateTo(Screen.DERM_AI) },
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = "DermAI") },
                        label = { Text("DermAI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SapphireBlue,
                            selectedTextColor = SapphireBlue,
                            indicatorColor = SapphireBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_derm_ai")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SKIN_QUIZ,
                        onClick = { viewModel.navigateTo(Screen.SKIN_QUIZ) },
                        icon = { Icon(Icons.Default.Face, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KlarityGoldPrimary,
                            selectedTextColor = KlarityGoldPrimary,
                            indicatorColor = KlarityGoldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_skin_profile")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SETTINGS_ABOUT,
                        onClick = { viewModel.navigateTo(Screen.SETTINGS_ABOUT) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "About & Settings") },
                        label = { Text("About", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KlarityGoldPrimary,
                            selectedTextColor = KlarityGoldPrimary,
                            indicatorColor = KlarityGoldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_settings_about")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    skinProfile = skinProfile,
                    recentScans = recentScans,
                    onNavigate = { viewModel.navigateTo(it) },
                    onRunPreset = { viewModel.runPreset(it) }
                )

                Screen.SCANNER -> ScannerScreen(
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onVerify = { barcode, nafdac, batch, location ->
                        viewModel.performVerification(barcode, nafdac, batch, location)
                    },
                    onAnalyzeIngredients = { text ->
                        viewModel.analyzeCustomIngredients(text)
                    },
                    onRunPreset = { viewModel.runPreset(it) }
                )

                Screen.RESULT -> VerificationResultScreen(
                    report = verificationReport,
                    ingredientAnalysis = ingredientAnalysis,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                Screen.DERM_AI -> DermAIChatScreen(
                    messages = chatMessages,
                    isLoading = isChatLoading,
                    skinProfile = skinProfile,
                    onSendMessage = { viewModel.sendDermAiMessage(it) },
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )

                Screen.SKIN_QUIZ -> SkinProfileQuizScreen(
                    currentProfile = skinProfile,
                    onSaveProfile = { viewModel.saveSkinProfile(it) },
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )

                Screen.REPORT_COUNTERFEIT -> CommunityReportScreen(
                    initialProduct = verificationReport?.product?.productName ?: "",
                    initialBrand = verificationReport?.product?.brandName ?: "",
                    initialBatch = verificationReport?.scannedBatchCode ?: "",
                    onSubmitReport = { product, brand, batch, market, city, details ->
                        viewModel.submitReport(product, brand, batch, market, city, details)
                    },
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )

                Screen.SETTINGS_ABOUT -> SettingsAboutScreen(
                    currentThemeMode = appThemeMode,
                    onSelectThemeMode = { viewModel.setAppThemeMode(it) },
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }

            // Global Loading Overlay
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC0A1128))
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, KlarityGoldPrimary),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = KlarityGoldPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ANALYZING AUTHENTICITY...",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Checking NAFDAC registry, batch syntax, and regional velocity trap.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
