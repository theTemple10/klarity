package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SkinProfile
import com.example.model.SkinType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LightSlate
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.StatusPassGreen

@Composable
fun SkinProfileQuizScreen(
    currentProfile: SkinProfile,
    onSaveProfile: (SkinProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedType by remember { mutableStateOf(currentProfile.skinType) }
    var isAcneProne by remember { mutableStateOf(currentProfile.isAcneProne) }
    var hasHyperpigmentation by remember { mutableStateOf(currentProfile.hasHyperpigmentation) }
    var isSensitive by remember { mutableStateOf(currentProfile.isSensitive) }

    val climates = listOf(
        "Tropical Humid (Lagos / Southern Nigeria)",
        "Arid Harmattan (Kano / Northern Nigeria)",
        "Moderate Plateau (Jos / Middle Belt)"
    )
    var selectedClimate by remember { mutableStateOf(currentProfile.climateZone) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBase)
            .testTag("skin_profile_quiz_screen")
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
                modifier = Modifier.testTag("quiz_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "SKIN INTELLIGENCE PROFILE",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = ElectricCyan)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Personalized Ingredient Profiler",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calibrates Klarity's AI cosmetic dermatologist to flag pore-clogging comedogenic ingredients, allergens, and tropical climate irritants.",
                            color = LightSlate,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1. Primary Skin Type
            Text(
                text = "1. SELECT PRIMARY SKIN TYPE",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            SkinType.entries.forEach { type ->
                val isSelected = selectedType == type
                Surface(
                    color = if (isSelected) SlateContainer else SlateSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ElectricCyan else SlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedType = type }
                        .testTag("skin_type_option_${type.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan else Color.Transparent)
                                .border(1.dp, if (isSelected) ElectricCyan else LightSlate, CircleShape)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ObsidianBase,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = type.displayName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = type.description,
                                color = LightSlate,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. Specific Target Concerns
            Text(
                text = "2. KEY DERMATOLOGICAL CONCERNS",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Acne-Prone & Pore Congestion", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Flags heavy comedogenic esters (scores 3-5)", color = LightSlate, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isAcneProne,
                            onCheckedChange = { isAcneProne = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBase,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Melanin Hyperpigmentation / PIH", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Highlights safe dark-spot faders & tyrosinase inhibitors", color = LightSlate, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hasHyperpigmentation,
                            onCheckedChange = { hasHyperpigmentation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBase,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sensitive & Reactive Skin", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Flags artificial fragrance, harsh alcohols, and allergens", color = LightSlate, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isSensitive,
                            onCheckedChange = { isSensitive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBase,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }
                }
            }

            // 3. Climate Zone
            Text(
                text = "3. REGIONAL CLIMATE CONTEXT",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            climates.forEach { climate ->
                val isSelected = selectedClimate == climate
                Surface(
                    color = if (isSelected) SlateContainer else SlateSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ElectricCyan else SlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedClimate = climate }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan else Color.Transparent)
                                .border(1.dp, if (isSelected) ElectricCyan else LightSlate, CircleShape)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianBase, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = climate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save CTA
            Button(
                onClick = {
                    val updated = SkinProfile(
                        skinType = selectedType,
                        isAcneProne = isAcneProne,
                        hasHyperpigmentation = hasHyperpigmentation,
                        isSensitive = isSensitive,
                        climateZone = selectedClimate,
                        allergens = listOf("Artificial Fragrance", "Parabens")
                    )
                    onSaveProfile(updated)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianBase
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_skin_profile_btn")
            ) {
                Text(
                    text = "SAVE SKIN PROFILE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
