package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.PresetScenario
import com.example.ui.components.CameraPermissionHandler
import com.example.ui.components.CameraPermissionRationaleView
import com.example.ui.components.QualityScoreMeter
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LightSlate
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.StatusAlertRed
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningYellow
import com.example.ui.theme.TealDark
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onVerify: (barcode: String, nafdac: String, batch: String, location: String) -> Unit,
    onAnalyzeIngredients: (ingredientsText: String) -> Unit,
    onRunPreset: (PresetScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Barcode/Batch, 1: Ingredient OCR, 2: Manual Data
    var isFlashOn by remember { mutableStateOf(false) }

    // Dynamic Frame Quality Simulation (Luminosity, Sharpness, Confidence)
    var luminosity by remember { mutableFloatStateOf(0.78f) }
    var sharpness by remember { mutableFloatStateOf(0.88f) }
    var confidence by remember { mutableFloatStateOf(0.89f) }

    // Form Fields
    var barcodeInput by remember { mutableStateOf("4005900940567") } // Nivea default
    var batchInput by remember { mutableStateOf("34819022") }
    var nafdacInput by remember { mutableStateOf("A2-4412") }
    var ingredientInput by remember {
        mutableStateOf("Aqua, Glycerin, Niacinamide, Pearl Extract, Sodium Ascorbyl Phosphate, Dimethicone")
    }

    val nigerianMarkets = listOf(
        "Lagos (Ikeja City Mall)",
        "Lagos (Balogun Market)",
        "Lagos (Idumota Wholesale)",
        "Kano (Kantin Kwari)",
        "Abuja (Wuse II Plaza)",
        "Port Harcourt (Mile 1)",
        "Onitsha (Main Market)",
        "Ibadan (Dugbe Mall)"
    )
    var selectedLocation by remember { mutableStateOf(nigerianMarkets[0]) }
    var locationExpanded by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Safe zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // When user picks packaging photo, simulate high-confidence OCR extraction
            barcodeInput = "4005900940567"
            batchInput = "34819022"
            nafdacInput = "A2-4412"
            confidence = 0.94f
        }
    }

    // Gentle live oscillation to demonstrate dynamic luminosity/sharpness sensor feedback
    LaunchedEffect(isFlashOn) {
        while (true) {
            delay(1500)
            luminosity = if (isFlashOn) 0.92f else (0.75f + (Math.random().toFloat() * 0.15f))
            sharpness = 0.85f + (Math.random().toFloat() * 0.12f)
            confidence = (luminosity + sharpness) / 2f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBase)
            .testTag("scanner_screen")
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
                modifier = Modifier.testTag("scanner_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "IMMERSIVE SCANNER",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = { isFlashOn = !isFlashOn },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isFlashOn) ElectricCyan else SlateSurfaceVariant)
                    .testTag("flash_toggle_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flashlight",
                    tint = if (isFlashOn) ObsidianBase else Color.White
                )
            }
        }

        // Mode Switcher Tabs
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
                text = { Text("BARCODE & BATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("INGREDIENTS (INCI)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("MANUAL INPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Viewport & Reticle Area (for Tab 0 and Tab 1) with runtime CameraPermissionHandler
            if (selectedTab == 0 || selectedTab == 1) {
                CameraPermissionHandler(
                    autoPrompt = false,
                    onManualEntryFallback = { selectedTab = 2 }
                ) { hasPermission, requestPermission ->
                    if (hasPermission) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SlateSurfaceVariant)
                                .border(1.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        ) {
                            // Reticle overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0x2266FCF1),
                                                Color(0x05000000),
                                                Color(0x2266FCF1)
                                            )
                                        )
                                    )
                            )

                            // Target Box
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(240.dp, 160.dp)
                                    .border(2.dp, ElectricCyan, RoundedCornerShape(12.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.QrCodeScanner else Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = ElectricCyan.copy(alpha = 0.7f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (selectedTab == 0) "ALIGN BARCODE OR BATCH STAMP" else "ALIGN INGREDIENT INCI LIST",
                                        color = ElectricCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // Floating Quality Meter
                            QualityScoreMeter(
                                luminosity = luminosity,
                                sharpness = sharpness,
                                confidence = confidence,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .fillMaxWidth(0.92f)
                            )
                        }
                    } else {
                        CameraPermissionRationaleView(
                            onRequestPermission = requestPermission,
                            onManualEntry = { selectedTab = 2 },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Photo Picker & Quick Capture Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("upload_packaging_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "UPLOAD PHOTO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                onVerify(barcodeInput, nafdacInput, batchInput, selectedLocation)
                            } else {
                                onAnalyzeIngredients(ingredientInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianBase
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("trigger_scan_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTab == 0) "VERIFY PACKAGING" else "ANALYZE INCI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Location Selector (Crucial for Velocity Trap Auditing across Lagos / Kano / Abuja)
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = !locationExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Purchase / Scan Location (Velocity Audit)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ElectricCyan)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SlateBorder,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = LightSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("scan_location_selector")
                )

                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false },
                    modifier = Modifier.background(SlateSurface)
                ) {
                    nigerianMarkets.forEach { market ->
                        DropdownMenuItem(
                            text = { Text(text = market, color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                selectedLocation = market
                                locationExpanded = false
                            }
                        )
                    }
                }
            }

            // Editable Target Fields
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedTab == 1) "INGREDIENT INCI LIST" else "DETECTED PACKAGING ATTRIBUTES",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "EDIT VALUES",
                            color = LightSlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = ingredientInput,
                            onValueChange = { ingredientInput = it },
                            label = { Text("Extracted Ingredients (Comma-separated)") },
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SlateBorder,
                                focusedLabelColor = ElectricCyan,
                                unfocusedLabelColor = LightSlate,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inci_ingredients_input")
                        )

                        Button(
                            onClick = { onAnalyzeIngredients(ingredientInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianBase
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_inci_btn")
                        ) {
                            Text("RUN CLINICAL INGREDIENT INTELLIGENCE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = { barcodeInput = it },
                            label = { Text("Barcode / GTIN Number") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SlateBorder,
                                focusedLabelColor = ElectricCyan,
                                unfocusedLabelColor = LightSlate,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("barcode_input")
                        )

                        OutlinedTextField(
                            value = batchInput,
                            onValueChange = { batchInput = it },
                            label = { Text("Batch Code Stamp (e.g. 34819022)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SlateBorder,
                                focusedLabelColor = ElectricCyan,
                                unfocusedLabelColor = LightSlate,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_input")
                        )

                        OutlinedTextField(
                            value = nafdacInput,
                            onValueChange = { nafdacInput = it },
                            label = { Text("NAFDAC Registration Number (e.g. A2-4412)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SlateBorder,
                                focusedLabelColor = ElectricCyan,
                                unfocusedLabelColor = LightSlate,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nafdac_input")
                        )

                        Button(
                            onClick = { onVerify(barcodeInput, nafdacInput, batchInput, selectedLocation) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianBase
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_verification_btn")
                        ) {
                            Text("RUN MULTI-LAYER VERIFICATION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Preset Bar for easy testing inside the scanner
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK FILL PACKAGING PRESETS:",
                    color = LightSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = SlateSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusPassGreen.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                barcodeInput = "4005900940567"
                                batchInput = "34819022"
                                nafdacInput = "A2-4412"
                            }
                            .padding(8.dp)
                    ) {
                        Text(text = "Nivea Authentic", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        color = SlateSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAlertRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                barcodeInput = "4005900940567"
                                batchInput = "98214432" // Cloned batch
                                nafdacInput = "A2-4412"
                            }
                            .padding(8.dp)
                    ) {
                        Text(text = "Cloned Batch Trap", color = StatusAlertRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
