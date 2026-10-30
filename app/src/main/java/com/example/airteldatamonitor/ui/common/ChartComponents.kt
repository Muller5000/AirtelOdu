package com.example.airteldatamonitor.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.CategoryUsage
import com.example.airteldatamonitor.data.model.UsageDataPoint
import com.example.airteldatamonitor.theme.NeonGreenAccent
import com.example.airteldatamonitor.theme.NeonGreenDim
import com.example.airteldatamonitor.theme.NeonGreenHighlight
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.theme.StatusWarning
import java.util.Locale

@Composable
fun UsageBarChart(
    dataPoints: List<UsageDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No usage recorded", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        return
    }

    val maxVal = remember(dataPoints) {
        val peak = dataPoints.maxOfOrNull { it.totalGb } ?: 1.0
        if (peak <= 0.0) 1.0 else peak * 1.15
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATA CONSUMPTION TREND",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreenAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusInfo)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = dataPoints.size
                val barSpacing = chartWidth / barCount
                val barWidth = (barSpacing * 0.5f).coerceIn(8.dp.toPx(), 28.dp.toPx())

                // Draw 3 horizontal subtle guide lines
                val gridSteps = 3
                for (i in 1..gridSteps) {
                    val y = chartHeight * (i.toFloat() / (gridSteps + 1))
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars
                dataPoints.forEachIndexed { index, point ->
                    val x = index * barSpacing + (barSpacing - barWidth) / 2f
                    val totalProgress = animationProgress.value

                    val downloadRatio = ((point.downloadGb / maxVal) * totalProgress).toFloat().coerceIn(0f, 1f)
                    val uploadRatio = ((point.uploadGb / maxVal) * totalProgress).toFloat().coerceIn(0f, 1f)

                    val downloadHeight = chartHeight * downloadRatio
                    val uploadHeight = chartHeight * uploadRatio

                    // Download Bar (bottom)
                    drawRoundRect(
                        color = NeonGreenAccent,
                        topLeft = Offset(x, chartHeight - downloadHeight),
                        size = Size(barWidth, downloadHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Upload Bar (stacked on top or slightly accented)
                    if (uploadHeight > 0f) {
                        drawRoundRect(
                            color = StatusInfo,
                            topLeft = Offset(x, chartHeight - downloadHeight - uploadHeight),
                            size = Size(barWidth, uploadHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEach { point ->
                    Text(
                        text = point.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryBreakdownView(
    categories: List<CategoryUsage>,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        NeonGreenAccent,
        StatusInfo,
        NeonGreenHighlight,
        StatusWarning,
        Color(0xFFA78BFA)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "TRAFFIC BREAKDOWN BY ACTIVITY",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                categories.forEachIndexed { index, cat ->
                    val color = palette[index % palette.size]
                    Box(
                        modifier = Modifier
                            .weight(cat.percentage.coerceAtLeast(0.1f))
                            .height(12.dp)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Legend items
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, cat ->
                    val color = palette[index % palette.size]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = cat.categoryName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f GB (%.0f%%)", cat.amountGb, cat.percentage),
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
