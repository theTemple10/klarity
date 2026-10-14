package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IngredientAnalysis
import com.example.model.VerificationReport
import com.example.model.VerificationStatus
import com.example.ui.Screen
import com.example.ui.components.IngredientCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.VerificationStepCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LightSlate
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.StatusAlertRed
import com.example.ui.theme.StatusAlertRedBg
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassGreenBg
import com.example.ui.theme.StatusWarningYellow
import com.example.ui.theme.StatusWarningYellowBg

@Composable
fun VerificationResultScreen(
    report: VerificationReport?,
    ingredientAnalysis: IngredientAnalysis?,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Authenticity Metrics, 1: Ingredient Safety Suite

    if (report == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBase)
        ) {
            Text("No active verification report found.", color = LightSlate)
        }
        return
    }

    val (verdictBg, verdictBorder, verdictIcon) = when (report.overallStatus) {
        VerificationStatus.GREEN -> Triple(StatusPassGreenBg, StatusPassGreen, Icons.Default.CheckCircle)
        VerificationStatus.YELLOW -> Triple(StatusWarningYellowBg, StatusWarningYellow, Icons.Default.Warning)
        VerificationStatus.RED -> Triple(StatusAlertRedBg, StatusAlertRed, Icons.Default.Error)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBase)
            .testTag("verification_result_screen")
    ) {
        // Top Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("result_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "AUDIT ANALYSIS SUITE",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = { onNavigate(Screen.SCANNER) },
                modifier = Modifier.testTag("scan_again_icon_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Scan New",
                    tint = ElectricCyan
                )
            }
        }

        // Categorized Tab Bar: Authenticity Metrics vs Ingredient Safety
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateSurface,
            contentColor = ElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ElectricCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AUTHENTICITY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INGREDIENT SAFETY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Prominent Verdict Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = verdictBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, verdictBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verdict_header_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = verdictIcon,
                                contentDescription = null,
                                tint = verdictBorder,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = report.statusHeadline,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        StatusBadge(status = report.overallStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = report.statusExplanation,
                        color = LightSlate,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    if (report.product != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = ObsidianBase.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "${report.product.brandName} - ${report.product.productName}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Official NAFDAC Reg: ${report.product.nafdacNumber}",
                                        color = ElectricCyan,
                                        fontSize = 11.sp
                                    )
                                }

                                if (report.product.isLocalBrand) {
                                    Surface(
                                        color = StatusPassGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusPassGreen)
                                    ) {
                                        Text(
                                            text = "MADE IN NIGERIA",
                                            color = StatusPassGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 0: AUTHENTICITY METRICS
            if (selectedTab == 0) {
                // 4-Layer Security Breakdown
                Text(
                    text = "THE 4-LAYER AUTHENTICITY AUDIT",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (report.steps.isNotEmpty()) {
                    report.steps.forEach { step ->
                        VerificationStepCard(step = step)
                    }
                }

                // Velocity & Geospatial Log Card (Cloned Batch Radar)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VELOCITY LOG & GEOGRAPHIC FOOTPRINT",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Batch Code Scanned: '${report.scannedBatchCode}'",
                            color = LightSlate,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Scanned In: ${report.scanLocation}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Active Retail Footprint: ${report.velocityLocations.joinToString(" • ")}",
                            color = if (report.velocityLocations.size > 1) StatusAlertRed else StatusPassGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (report.velocityLocations.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🚨 Cloned batch alert triggered: This batch imprint is circulating in multiple commercial markets simultaneously.",
                                color = StatusAlertRed,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // TAB 1: INGREDIENT SAFETY SUITE
            if (selectedTab == 1) {
                if (ingredientAnalysis == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No ingredient list parsed for this product yet. Tap 'Open Immersive Scanner' and select 'Ingredients (INCI)' to parse a container label.",
                                color = LightSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Clinical Score Banner
                    val scoreColor = when (ingredientAnalysis.overallScore) {
                        "EXCELLENT" -> StatusPassGreen
                        "DANGEROUS" -> StatusAlertRed
                        "USE_WITH_CAUTION" -> StatusWarningYellow
                        else -> ElectricCyan
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CLINICAL DERMATOLOGY SCORE",
                                    color = scoreColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Surface(
                                    color = scoreColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor)
                                ) {
                                    Text(
                                        text = ingredientAnalysis.overallScore,
                                        color = scoreColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = ingredientAnalysis.summary,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Toxic / Bleach Hazard Banner
                    if (ingredientAnalysis.toxicWarnings.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusAlertRedBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusAlertRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = StatusAlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "HAZARDOUS TOXIC CHEMICALS DETECTED",
                                        color = StatusAlertRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                ingredientAnalysis.toxicWarnings.forEach { warning ->
                                    Text(
                                        text = "• $warning",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // INCI Breakdown List
                    Text(
                        text = "INCI INGREDIENT BREAKDOWN (${ingredientAnalysis.ingredients.size} ANALYZED)",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    ingredientAnalysis.ingredients.forEach { detail ->
                        IngredientCard(detail = detail)
                    }
                }
            }

            // Bottom Actions: Ask DermAI & Report Counterfeit
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.DERM_AI) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianBase
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("ask_derm_ai_action_btn")
                ) {
                    Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONSULT KLARITY DERMAI ASSISTANT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (report.overallStatus == VerificationStatus.RED || report.overallStatus == VerificationStatus.YELLOW) {
                    OutlinedButton(
                        onClick = { onNavigate(Screen.REPORT_COUNTERFEIT) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAlertRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAlertRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("report_from_result_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FLAG THIS PRODUCT TO COMMUNITY / NAFDAC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
