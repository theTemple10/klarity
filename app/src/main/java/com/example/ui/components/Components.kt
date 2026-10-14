package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IngredientDetail
import com.example.model.VerificationStatus
import com.example.model.VerificationStep
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LightSlate
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.StatusAlertRed
import com.example.ui.theme.StatusAlertRedBg
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassGreenBg
import com.example.ui.theme.StatusWarningYellow
import com.example.ui.theme.StatusWarningYellowBg
import com.example.ui.theme.ToxicPurple

@Composable
fun StatusBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, icon, label) = when (status) {
        VerificationStatus.GREEN -> Quintuple(
            StatusPassGreenBg,
            StatusPassGreen,
            StatusPassGreen,
            Icons.Default.CheckCircle,
            "VERIFIED AUTHENTIC"
        )
        VerificationStatus.YELLOW -> Quintuple(
            StatusWarningYellowBg,
            StatusWarningYellow,
            StatusWarningYellow,
            Icons.Default.Warning,
            "UNVERIFIED PACKAGING"
        )
        VerificationStatus.RED -> Quintuple(
            StatusAlertRedBg,
            StatusAlertRed,
            StatusAlertRed,
            Icons.Default.Error,
            "FRAUD / COUNTERFEIT ALERT"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
        modifier = modifier.testTag("status_badge_${status.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun VerificationStepCard(
    step: VerificationStep,
    modifier: Modifier = Modifier
) {
    val statusColor = when (step.status) {
        VerificationStatus.GREEN -> StatusPassGreen
        VerificationStatus.YELLOW -> StatusWarningYellow
        VerificationStatus.RED -> StatusAlertRed
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SlateSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (step.status == VerificationStatus.RED) StatusAlertRed.copy(alpha = 0.8f) else SlateBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("step_card_${step.stepNumber}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f))
                            .border(1.dp, statusColor, CircleShape)
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(status = step.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.details,
                color = LightSlate,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (!step.flaggedIssue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusAlertRedBg)
                        .border(1.dp, StatusAlertRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = StatusAlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.flaggedIssue,
                            color = StatusAlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientCard(
    detail: IngredientDetail,
    modifier: Modifier = Modifier
) {
    val (actionBg, actionText, actionLabel) = when (detail.action) {
        "PRO" -> Triple(StatusPassGreenBg, StatusPassGreen, "BENEFICIAL")
        "CON" -> Triple(StatusWarningYellowBg, StatusWarningYellow, "POTENTIAL IRRITANT")
        "TOXIC_ALERT" -> Triple(StatusAlertRedBg, StatusAlertRed, "TOXIC / BANNED")
        else -> Triple(SlateSurfaceVariant, LightSlate, "NEUTRAL")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (detail.isToxicBleach) StatusAlertRed else SlateBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ingredient_item_${detail.name.take(10)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = detail.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = actionBg,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, actionText.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = actionLabel,
                        color = actionText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = detail.explanation,
                color = LightSlate,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (detail.comedogenicScore != null && detail.comedogenicScore > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Comedogenic (Pore-Clogging) Index: ${detail.comedogenicScore}/5",
                        color = if (detail.comedogenicScore >= 4) StatusAlertRed else LightSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { detail.comedogenicScore / 5f },
                        color = if (detail.comedogenicScore >= 4) StatusAlertRed else if (detail.comedogenicScore >= 2) StatusWarningYellow else StatusPassGreen,
                        trackColor = SlateBorder,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun QualityScoreMeter(
    luminosity: Float,
    sharpness: Float,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val isDark = luminosity < 0.35f
    val isBlurry = sharpness < 0.45f

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBase.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
        modifier = modifier.testTag("quality_score_meter")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FRAME QUALITY RADAR",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${(confidence * 100).toInt()}% READY",
                    color = if (confidence >= 0.85f) StatusPassGreen else StatusWarningYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { confidence },
                color = if (confidence >= 0.85f) StatusPassGreen else ElectricCyan,
                trackColor = SlateBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            AnimatedVisibility(visible = isDark || isBlurry) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    if (isDark) {
                        Text(
                            text = "⚠ Too Dark - Turn on Flashlight / Increase Lighting",
                            color = StatusWarningYellow,
                            fontSize = 11.sp
                        )
                    }
                    if (isBlurry) {
                        Text(
                            text = "⚠ Image Blurry - Hold Device Steady over Packaging",
                            color = StatusAlertRed,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
