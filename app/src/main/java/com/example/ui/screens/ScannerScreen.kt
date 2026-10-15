package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import android.graphics.BitmapFactory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.scanner.PackagingOcrAnalyzer
import com.example.ui.PresetScenario
import com.example.ui.components.CameraViewfinder
import com.example.ui.components.NigerianLocationSelector
import com.example.ui.theme.KlarityGoldPrimary
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.StatusAlertRed
import com.example.ui.theme.StatusPassGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onVerify: (barcode: String, nafdac: String, batch: String, location: String) -> Unit,
    onAnalyzeIngredients: (ingredientsText: String) -> Unit,
    onRunPreset: (PresetScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Barcode/Batch, 1: Ingredient INCI, 2: Manual Input
    var isFlashOn by remember { mutableStateOf(false) }

    // Persistent Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    // Form inputs — empty by default, NO misleading hardcoded strings
    var barcodeInput by remember { mutableStateOf("") }
    var batchInput by remember { mutableStateOf("") }
    var nafdacInput by remember { mutableStateOf("") }
    var ingredientInput by remember { mutableStateOf("") }

    // Validation warning message state
    var validationError by remember { mutableStateOf<String?>(null) }

    // Captured image state
    var capturedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCapturedSuccessBanner by remember { mutableStateOf(false) }
    var isExtractingFromImage by remember { mutableStateOf(false) }
    var extractionSummary by remember { mutableStateOf<String?>(null) }

    // State & Landmark Selection (supports all 36 States + FCT and customizable landmark)
    var selectedState by remember { mutableStateOf("Lagos") }
    var selectedLandmark by remember { mutableStateOf("Ikeja City Mall") }

    val fullLocationString = if (selectedLandmark.isNotBlank()) "$selectedState ($selectedLandmark)" else selectedState

    // Zero-permission Photo Picker with automated OCR pipeline
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showCapturedSuccessBanner = true
            validationError = null
            coroutineScope.launch {
                isExtractingFromImage = true
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    if (bitmap != null) {
                        capturedPhotoBitmap = bitmap
                        val result = withContext(Dispatchers.Default) {
                            PackagingOcrAnalyzer.analyzeBitmap(bitmap)
                        }
                        val found = mutableListOf<String>()
                        result.gtin?.value?.let {
                            barcodeInput = it
                            found.add("Barcode: $it")
                        }
                        result.nafdacNo?.value?.let {
                            nafdacInput = it
                            found.add("NAFDAC: $it")
                        }
                        result.batchCode?.value?.let {
                            batchInput = it
                            found.add("Batch: $it")
                        }
                        if (result.ingredients.isNotEmpty()) {
                            ingredientInput = result.ingredients.joinToString(", ")
                            found.add("${result.ingredients.size} ingredients")
                        }
                        if (found.isNotEmpty()) {
                            extractionSummary = "Extracted from photo: ${found.joinToString(" • ")}"
                        } else {
                            extractionSummary = "Photo uploaded. Enter any missing identifiers below."
                        }
                    }
                } catch (_: Exception) {
                    extractionSummary = "Photo uploaded. Ready to verify."
                } finally {
                    isExtractingFromImage = false
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "PACKAGING SCANNER",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = { isFlashOn = !isFlashOn },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isFlashOn) KlarityGoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("flash_toggle_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flashlight",
                    tint = if (isFlashOn) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Clean Mode Selector Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = KlarityGoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = KlarityGoldPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    validationError = null
                },
                text = { Text("BARCODE & BATCH", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    validationError = null
                },
                text = { Text("INGREDIENTS (INCI)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    validationError = null
                },
                text = { Text("MANUAL INPUT", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Hardware Camera Viewfinder (for Tab 0 and Tab 1)
            if (selectedTab == 0 || selectedTab == 1) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, KlarityGoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    ) {
                        CameraViewfinder(
                            isFlashOn = isFlashOn,
                            onToggleFlash = { isFlashOn = !isFlashOn },
                            guidanceText = if (selectedTab == 0)
                                "Center barcode or batch stamp • Live auto-scan or tap shutter"
                            else
                                "Center cosmetic ingredient list • Tap shutter to capture",
                            onBarcodeDetected = { detectedBarcode ->
                                if (barcodeInput != detectedBarcode) {
                                    barcodeInput = detectedBarcode
                                    extractionSummary = "Live Barcode Auto-Detected: $detectedBarcode"
                                    showCapturedSuccessBanner = true
                                    validationError = null
                                }
                            },
                            onImageCaptured = { bitmap ->
                                capturedPhotoBitmap = bitmap
                                showCapturedSuccessBanner = true
                                validationError = null
                                coroutineScope.launch {
                                    isExtractingFromImage = true
                                    try {
                                        val result = withContext(Dispatchers.Default) {
                                            PackagingOcrAnalyzer.analyzeBitmap(bitmap)
                                        }
                                        val found = mutableListOf<String>()
                                        result.gtin?.value?.let {
                                            barcodeInput = it
                                            found.add("Barcode: $it")
                                        }
                                        result.nafdacNo?.value?.let {
                                            nafdacInput = it
                                            found.add("NAFDAC: $it")
                                        }
                                        result.batchCode?.value?.let {
                                            batchInput = it
                                            found.add("Batch: $it")
                                        }
                                        if (result.ingredients.isNotEmpty()) {
                                            ingredientInput = result.ingredients.joinToString(", ")
                                            found.add("${result.ingredients.size} ingredients")
                                        }
                                        if (found.isNotEmpty()) {
                                            extractionSummary = "Extracted from photo: ${found.joinToString(" • ")}"
                                        } else {
                                            extractionSummary = "Photo processed. No barcode or NAFDAC detected. Enter values manually below."
                                        }
                                    } catch (e: Exception) {
                                        extractionSummary = "Captured photo ready. Verify or refine values below."
                                    } finally {
                                        isExtractingFromImage = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Minimalist, Clean Permission Request Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(KlarityGoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = KlarityGoldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Camera Scanner",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Grant camera access to scan packaging barcodes, detect batch stamps, and read ingredient lists in real time.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KlarityGoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("enable_camera_btn")
                            ) {
                                Text("ALLOW CAMERA ACCESS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Photo Captured Success Pill / Thumbnail & Extraction Progress
                AnimatedVisibility(visible = showCapturedSuccessBanner) {
                    Surface(
                        color = StatusPassGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusPassGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isExtractingFromImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp,
                                        color = KlarityGoldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                } else if (capturedPhotoBitmap != null) {
                                    Image(
                                        bitmap = capturedPhotoBitmap!!.asImageBitmap(),
                                        contentDescription = "Captured Packaging",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusPassGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Column {
                                    Text(
                                        text = if (isExtractingFromImage) "Analyzing Packaging OCR..." else "Packaging Photo Processed",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = extractionSummary ?: "Identifiers extracted. Verify or adjust values below.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showCapturedSuccessBanner = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SapphireBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("upload_packaging_photo_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "UPLOAD PHOTO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                if (barcodeInput.isBlank() && nafdacInput.isBlank() && batchInput.isBlank()) {
                                    validationError = "Please capture packaging with camera, upload photo, or enter Barcode / NAFDAC Reg No below to verify."
                                } else {
                                    validationError = null
                                    onVerify(barcodeInput, nafdacInput, batchInput, fullLocationString)
                                }
                            } else {
                                if (ingredientInput.isBlank()) {
                                    validationError = "Please enter or capture ingredient text before running toxicology analysis."
                                } else {
                                    validationError = null
                                    onAnalyzeIngredients(ingredientInput)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KlarityGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("trigger_scan_action_btn")
                    ) {
                        Text(
                            text = if (selectedTab == 0) "VERIFY PACKAGING" else "ANALYZE INGREDIENTS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Inline Validation Error Alert
            AnimatedVisibility(visible = validationError != null) {
                Surface(
                    color = StatusAlertRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusAlertRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusAlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Intuitive State & Landmark Location Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PURCHASE LOCATION AUDIT",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select your state and closest landmark or store to cross-check regional cloned batch reports.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    NigerianLocationSelector(
                        selectedState = selectedState,
                        onStateSelected = { selectedState = it },
                        landmarkInput = selectedLandmark,
                        onLandmarkChanged = { selectedLandmark = it }
                    )
                }
            }

            // Packaging Attributes & Form Fields (Real inputs, empty by default)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (selectedTab == 1) "COSMETIC INGREDIENTS (INCI)" else "PACKAGING IDENTIFIERS",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = ingredientInput,
                            onValueChange = {
                                ingredientInput = it
                                validationError = null
                            },
                            placeholder = { Text("e.g. Aqua, Glycerin, Niacinamide, Shea Butter, Cetearyl Alcohol...", fontSize = 13.sp) },
                            label = { Text("Extracted Ingredient List (Comma-separated)", fontSize = 13.sp) },
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SapphireBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = SapphireBlue,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inci_ingredients_input")
                        )

                        Button(
                            onClick = {
                                if (ingredientInput.isBlank()) {
                                    validationError = "Please enter or capture ingredient text before running toxicology analysis."
                                } else {
                                    validationError = null
                                    onAnalyzeIngredients(ingredientInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SapphireBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_inci_btn")
                        ) {
                            Text("RUN INGREDIENT INTELLIGENCE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = {
                                barcodeInput = it
                                validationError = null
                            },
                            placeholder = { Text("e.g. 4005900940567", fontSize = 13.sp) },
                            label = { Text("Barcode / GTIN Number", fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KlarityGoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = KlarityGoldPrimary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("barcode_input")
                        )

                        OutlinedTextField(
                            value = batchInput,
                            onValueChange = {
                                batchInput = it
                                validationError = null
                            },
                            placeholder = { Text("e.g. 34819022, 20W301", fontSize = 13.sp) },
                            label = { Text("Batch Code (Stamped on bottle)", fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KlarityGoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = KlarityGoldPrimary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_input")
                        )

                        OutlinedTextField(
                            value = nafdacInput,
                            onValueChange = {
                                nafdacInput = it
                                validationError = null
                            },
                            placeholder = { Text("e.g. A2-4412", fontSize = 13.sp) },
                            label = { Text("Registration Number (Printed on packaging)", fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KlarityGoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = KlarityGoldPrimary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nafdac_input")
                        )

                        Button(
                            onClick = {
                                if (barcodeInput.isBlank() && nafdacInput.isBlank() && batchInput.isBlank()) {
                                    validationError = "Please enter at least a Barcode or NAFDAC Reg No to run verification."
                                } else {
                                    validationError = null
                                    onVerify(barcodeInput, nafdacInput, batchInput, fullLocationString)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KlarityGoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_verification_btn")
                        ) {
                            Text("RUN AUTHENTICITY VERIFICATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Test Suite / Sample Packaging Test Library (Clean, transparent, labeled as test benchmarks)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TEST PACKAGING BENCHMARKS",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "If testing without physical packaging in hand, tap any registered benchmark to evaluate authentic vs counterfeit detection paths.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SamplePackagingRow(
                            label = "Nivea Radiant & Beauty Adv. Care (4005900925022 / A2-101409)",
                            statusColor = StatusPassGreen,
                            onClick = {
                                barcodeInput = "4005900925022"
                                nafdacInput = "A2-101409"
                                batchInput = "24189021"
                                validationError = null
                            }
                        )
                        SamplePackagingRow(
                            label = "Authentic Nivea Lotion (Pass)",
                            statusColor = StatusPassGreen,
                            onClick = {
                                barcodeInput = "4005900940567"
                                nafdacInput = "A2-4412"
                                batchInput = "34819022"
                                validationError = null
                            }
                        )
                        SamplePackagingRow(
                            label = "CeraVe Tampered NAFDAC (Fraud Warning)",
                            statusColor = StatusAlertRed,
                            onClick = {
                                barcodeInput = "3337875597371"
                                nafdacInput = "FAKE-9999"
                                batchInput = "20W301"
                                validationError = null
                            }
                        )
                        SamplePackagingRow(
                            label = "Cloned Batch Code (Regional Velocity Trap)",
                            statusColor = StatusAlertRed,
                            onClick = {
                                barcodeInput = "4005900940567"
                                nafdacInput = "A2-4412"
                                batchInput = "98214432"
                                validationError = null
                            }
                        )
                        SamplePackagingRow(
                            label = "Shea Origin Body Butter (Made in Nigeria Pass)",
                            statusColor = StatusPassGreen,
                            onClick = {
                                barcodeInput = "6151101928014"
                                nafdacInput = "A4-2281"
                                batchInput = "SO-LA-2024"
                                validationError = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SamplePackagingRow(
    label: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "FILL",
                color = SapphireBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
