package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KlarityGoldPrimary
import com.example.ui.theme.SapphireBlue

val NigerianStates = listOf(
    "Lagos", "Abuja (FCT)", "Kano", "Rivers", "Oyo", "Anambra", "Enugu", "Kaduna", "Edo", "Ogun",
    "Delta", "Abia", "Akwa Ibom", "Bauchi", "Bayelsa", "Benue", "Borno", "Cross River",
    "Ebonyi", "Ekiti", "Gombe", "Imo", "Jigawa", "Katsina", "Kebbi", "Kogi", "Kwara",
    "Nasarawa", "Niger", "Ondo", "Osun", "Plateau", "Sokoto", "Taraba", "Yobe", "Zamfara"
)

val PopularLandmarksByState = mapOf(
    "Lagos" to listOf("Ikeja City Mall", "Balogun Market", "Trade Fair Complex", "Spar Lekki", "Idumota Wholesale", "Yaba Tech", "Surulere"),
    "Abuja (FCT)" to listOf("Wuse 2 Plaza", "Banex Plaza", "Garki Market", "Next Cash & Carry", "Jabi Lake Mall", "Maitama"),
    "Kano" to listOf("Kantin Kwari Market", "Sabon Gari Market", "Ahmadu Bello Way", "Kano Mall"),
    "Rivers" to listOf("Mile 1 Market", "GRA Phase 2", "Spar Port Harcourt", "Trans-Amadi", "Oil Mill Market"),
    "Oyo" to listOf("Dugbe Mall", "Bodija Market", "Ring Road Shoprite", "Iwo Road"),
    "Anambra" to listOf("Onitsha Main Market", "Nnewi Auto Market", "Awka Mall"),
    "Enugu" to listOf("Polo Park Mall", "Ogbete Main Market", "Independence Layout"),
    "Kaduna" to listOf("Kaduna Central Market", "Barnawa", "Ahmadu Bello Stadium area"),
    "Ogun" to listOf("Abeokuta Mall", "Kuto Market", "Ota Industrial Zone")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NigerianLocationSelector(
    selectedState: String,
    onStateSelected: (String) -> Unit,
    landmarkInput: String,
    onLandmarkChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    val suggestedLandmarks = PopularLandmarksByState[selectedState] ?: listOf("Central Market", "Main City Center", "Commercial Area", "Shopping Plaza")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // State Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = stateDropdownExpanded,
            onExpandedChange = { stateDropdownExpanded = !stateDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedState,
                onValueChange = {},
                readOnly = true,
                label = { Text("State / Region (Nigeria)", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = KlarityGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateDropdownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlarityGoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = KlarityGoldPrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("state_selector_input")
            )

            ExposedDropdownMenu(
                expanded = stateDropdownExpanded,
                onDismissRequest = { stateDropdownExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                NigerianStates.forEach { state ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = state,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (state == selectedState) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onStateSelected(state)
                            stateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Closest Landmark / Market / Store Input
        OutlinedTextField(
            value = landmarkInput,
            onValueChange = onLandmarkChanged,
            label = { Text("Closest Landmark, Market, or Store Name", fontSize = 13.sp) },
            placeholder = { Text("e.g. Ikeja City Mall, Balogun Market, Shoprite, Medplus, Local Chemist", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = SapphireBlue,
                    modifier = Modifier.size(20.dp)
                )
            },
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
                .testTag("landmark_custom_input")
        )

        // Popular landmark quick chips for the selected state
        Column {
            Text(
                text = "POPULAR IN $selectedState (TAP TO SELECT):",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestedLandmarks.forEach { landmark ->
                    Surface(
                        color = if (landmarkInput == landmark) KlarityGoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (landmarkInput == landmark) KlarityGoldPrimary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clickable { onLandmarkChanged(landmark) }
                            .testTag("landmark_chip_${landmark.replace(" ", "_")}")
                    ) {
                        Text(
                            text = landmark,
                            color = if (landmarkInput == landmark) KlarityGoldPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
