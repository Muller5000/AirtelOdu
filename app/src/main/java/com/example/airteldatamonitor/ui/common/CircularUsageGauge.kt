package com.example.airteldatamonitor.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusWarning
import java.util.Locale

/**
 * Editorial Hero Data Balance Card.
 * Designed with concentric radii (R_outer=24, R_inner=16, R_pill=10),
 * specular rim highlight, tabular numerals ("tnum"), and high-contrast typography.
 */
@Composable
fun HeroDataCard(
    dataUsage: DataUsage,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetSweep = (dataUsage.usagePercentage / 100f).coerceIn(0f, 1f)

    LaunchedEffect(targetSweep) {
        animatedProgress.animateTo(
            targetValue = targetSweep,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header Row: Plan name & days remaining pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                    Text(
                        text = "DATA REMAINING",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dataUsage.planName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                // Days remaining pill (concentric R = 10.dp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(StatusOnline)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${dataUsage.daysRemainingInCycle} days left",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hero Figure: Large tabular numeric display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", dataUsage.remainingGb),
                    style = DataTypography.heroMetric,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GB",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.1f%% used", dataUsage.usagePercentage),
                        style = DataTypography.labelCaps,
                        color = AirtelRed
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f of %.0f GB", dataUsage.usedGb, dataUsage.totalPlanGb),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sleek Progress Track (R = 4.dp, height = 7.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AirtelRed, Color(0xFFFF4D4D))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Concentric Telemetry Bar (R_outer = 14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryCol(
                    label = "DAILY AVG",
                    value = String.format(Locale.US, "%.1f GB/d", dataUsage.dailyAverageGb),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                TelemetryCol(
                    label = "PROJECTED",
                    value = String.format(Locale.US, "%.0f GB", dataUsage.projectedMonthEndGb),
                    valueColor = if (dataUsage.projectedMonthEndGb > dataUsage.totalPlanGb) StatusWarning else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                TelemetryCol(
                    label = "HEALTH",
                    value = if (dataUsage.projectedMonthEndGb > dataUsage.totalPlanGb) "Heavy" else "Optimal",
                    valueColor = if (dataUsage.projectedMonthEndGb > dataUsage.totalPlanGb) StatusWarning else StatusOnline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryCol(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = DataTypography.labelCaps,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = DataTypography.metricMedium,
            color = valueColor,
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Backward compatibility wrapper for CircularUsageGauge
 */
@Composable
fun CircularUsageGauge(
    dataUsage: DataUsage,
    modifier: Modifier = Modifier
) {
    HeroDataCard(dataUsage = dataUsage, modifier = modifier)
}
