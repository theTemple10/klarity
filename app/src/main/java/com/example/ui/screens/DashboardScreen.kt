package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ScanLogEntity
import com.example.model.SkinProfile
import com.example.model.VerificationStatus
import com.example.ui.PresetScenario
import com.example.ui.Screen
import com.example.ui.components.StatusBadge
import com.example.ui.theme.KlarityGoldDark
import com.example.ui.theme.KlarityGoldLight
import com.example.ui.theme.KlarityGoldPrimary
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SapphireBlue
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
import com.example.ui.theme.StatusWarningYellow
import com.example.ui.theme.TealDark

@Composable
fun DashboardScreen(
    skinProfile: SkinProfile,
    recentScans: List<ScanLogEntity>,
    onNavigate: (Screen) -> Unit,
    onRunPreset: (PresetScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen")
    ) {
        // Top App Header with Bespoke Mascot Emblem
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigate(Screen.SETTINGS_ABOUT) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(KlarityGoldPrimary.copy(alpha = 0.3f), SapphireBlue.copy(alpha = 0.2f))
                                )
                            )
                            .border(1.5.dp, KlarityGoldPrimary, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_klarity_fun_logo_1791978487683),
                            contentDescription = "Klarity Mascot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KLARITY",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = KlarityGoldPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KlarityGoldPrimary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "NAFDAC SHIELD",
                                    color = KlarityGoldPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "FMCG Authenticity & Clinical Intelligence",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onNavigate(Screen.SETTINGS_ABOUT) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, KlarityGoldPrimary.copy(alpha = 0.5f), CircleShape)
                            .testTag("settings_header_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & About",
                            tint = KlarityGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(Screen.DERM_AI) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, SapphireBlue.copy(alpha = 0.6f), CircleShape)
                            .testTag("derm_ai_header_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "DermAI Assistant",
                            tint = SapphireBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Hero Camera Scan CTA
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KlarityGoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.SCANNER) }
                    .testTag("scan_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(KlarityGoldPrimary.copy(alpha = 0.14f), SapphireBlue.copy(alpha = 0.04f))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(KlarityGoldPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Scanner",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Scan Packaging",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Barcode • NAFDAC • Batch • INCI",
                                        color = SapphireBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = KlarityGoldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Cross-references central cosmetics ledger, validates brand batch syntax, and activates our regional Cloned Batch Trap across Lagos, Kano, and Abuja.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onNavigate(Screen.SCANNER) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KlarityGoldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("launch_camera_scanner_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OPEN IMMERSIVE SCANNER",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // User Skin Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skin_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YOUR SKIN PROFILE",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "EDIT PROFILE",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigate(Screen.SKIN_QUIZ) }
                                .padding(4.dp)
                                .testTag("edit_skin_profile_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = SlateContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Type", color = LightSlate, fontSize = 10.sp)
                                Text(
                                    text = skinProfile.skinType.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = SlateContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Target Concerns", color = LightSlate, fontSize = 10.sp)
                                Text(
                                    text = if (skinProfile.hasHyperpigmentation) "Dark Spots / PIH" else "Acne Clearing",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Climate: ${skinProfile.climateZone}",
                        color = LightSlate,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Market Risk Radar (Nigerian Market Alerts)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusAlertRed.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_risk_radar")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = StatusAlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NIGERIAN MARKET VELOCITY RADAR",
                            color = StatusAlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• ACTIVE CLONED BATCH: Batch #98214432 (Nivea Even Glow) detected concurrently across Idumota Market (Lagos) and Kantin Kwari (Kano).\n• REGULATORY NOTICE: NAFDAC recall on unindexed mercury bleaching lotions in Trade Fair Complex.",
                        color = LightSlate,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onNavigate(Screen.REPORT_COUNTERFEIT) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAlertRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAlertRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("report_counterfeit_entry_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REPORT COUNTERFEIT VENDOR TO COMMUNITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Testing Scenarios (Instant 1-Tap Verification Sandbox)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "VERIFICATION TEST SUITE (ONE-TAP DEMOS)",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tap any test case below to inspect the multi-layer security engine in action:",
                    color = LightSlate,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DemoPresetButton(
                        title = "Authentic Nivea",
                        subtitle = "Passes all 4 layers",
                        status = VerificationStatus.GREEN,
                        onClick = { onRunPreset(PresetScenario.NIVEA_AUTHENTIC) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoPresetButton(
                        title = "Cloned Batch Trap",
                        subtitle = "Lagos vs Kano anomaly",
                        status = VerificationStatus.RED,
                        onClick = { onRunPreset(PresetScenario.CLONED_BATCH_TRAP) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DemoPresetButton(
                        title = "Tampered NAFDAC",
                        subtitle = "Token mismatch",
                        status = VerificationStatus.RED,
                        onClick = { onRunPreset(PresetScenario.CERAVE_NAFDAC_TAMPERED) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoPresetButton(
                        title = "Unverified FMCG",
                        subtitle = "Logged to community",
                        status = VerificationStatus.YELLOW,
                        onClick = { onRunPreset(PresetScenario.UNVERIFIED_COMMUNITY) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DemoPresetButton(
                        title = "Toxic Bleach Alert",
                        subtitle = "Steroid & Mercury check",
                        status = VerificationStatus.RED,
                        onClick = { onRunPreset(PresetScenario.TOXIC_BLEACH_CREAM) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoPresetButton(
                        title = "Nigerian Brand",
                        subtitle = "Shea Origin Virgin Butter",
                        status = VerificationStatus.GREEN,
                        onClick = { onRunPreset(PresetScenario.SHEA_ORIGIN_AUTHENTIC) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Scans Section
        item {
            Text(
                text = "RECENT VERIFICATION LOGS",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (recentScans.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No recent scans yet. Tap 'Open Immersive Scanner' above to scan your first product.",
                            color = LightSlate,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(recentScans.take(5)) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.brandName} - ${log.productName}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Batch: ${log.batchCodeScanned} • Location: ${log.stateLocation}",
                                color = LightSlate,
                                fontSize = 11.sp
                            )
                            Text(
                                text = log.statusReason,
                                color = if (log.status == "GREEN") StatusPassGreen else if (log.status == "YELLOW") StatusWarningYellow else StatusAlertRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        StatusBadge(
                            status = when (log.status) {
                                "GREEN" -> VerificationStatus.GREEN
                                "YELLOW" -> VerificationStatus.YELLOW
                                else -> VerificationStatus.RED
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DemoPresetButton(
    title: String,
    subtitle: String,
    status: VerificationStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (status) {
        VerificationStatus.GREEN -> StatusPassGreen.copy(alpha = 0.5f)
        VerificationStatus.YELLOW -> StatusWarningYellow.copy(alpha = 0.5f)
        VerificationStatus.RED -> StatusAlertRed.copy(alpha = 0.5f)
    }

    Surface(
        color = SlateSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clickable { onClick() }
            .testTag("preset_${title.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = LightSlate,
                fontSize = 10.sp
            )
        }
    }
}
