package com.example.airteldatamonitor.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.speedtest.SpeedTestStage
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.NeonGreenAccent
import com.example.airteldatamonitor.theme.NeonGreenGlow
import com.example.airteldatamonitor.theme.NeonGreenHighlight
import com.example.airteldatamonitor.theme.StatusError
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.ui.common.tactileClickable
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.testState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val routerInfo by viewModel.routerInfo.collectAsState()

    val isRunning = state.stage == SpeedTestStage.PING ||
            state.stage == SpeedTestStage.DOWNLOAD ||
            state.stage == SpeedTestStage.UPLOAD

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SpeedTestHeader()
            }

            // Speedometer Centerpiece
            item {
                SpeedometerGauge(
                    currentSpeedMbps = state.currentSpeedMbps,
                    stage = state.stage,
                    progress = state.progress
                )
            }

            // Realtime Sparkline (during active tests)
            if (state.speedHistory.isNotEmpty()) {
                item {
                    SpeedHistorySparkline(
                        history = state.speedHistory,
                        stage = state.stage
                    )
                }
            }

            // Results Grid: Download, Upload, Ping, Jitter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpeedMetricCard(
                        title = "DOWNLOAD",
                        value = state.downloadSpeedMbps?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        unit = "Mbps",
                        subtitle = state.peakDownloadMbps?.let { "Peak: $it Mbps" } ?: "Max speed",
                        tint = NeonGreenAccent,
                        isActive = state.stage == SpeedTestStage.DOWNLOAD,
                        modifier = Modifier.weight(1f)
                    )

                    SpeedMetricCard(
                        title = "UPLOAD",
                        value = state.uploadSpeedMbps?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        unit = "Mbps",
                        subtitle = state.peakUploadMbps?.let { "Peak: $it Mbps" } ?: "Max speed",
                        tint = StatusInfo,
                        isActive = state.stage == SpeedTestStage.UPLOAD,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpeedMetricCard(
                        title = "LATENCY (PING)",
                        value = state.pingMs?.toString() ?: "--",
                        unit = "ms",
                        subtitle = "Round-trip time",
                        tint = MaterialTheme.colorScheme.onSurface,
                        isActive = state.stage == SpeedTestStage.PING,
                        modifier = Modifier.weight(1f)
                    )

                    SpeedMetricCard(
                        title = "JITTER",
                        value = state.jitterMs?.toString() ?: "--",
                        unit = "ms",
                        subtitle = "Ping stability",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        isActive = state.stage == SpeedTestStage.PING,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Cellular Hardware Context
            routerInfo?.let { info ->
                item {
                    HardwareContextCard(
                        modelName = info.modelName,
                        band = info.primaryBand,
                        rsrp = "${info.signal.rsrpDbm} dBm",
                        operator = "Airtel Nigeria"
                    )
                }
            }

            // Action Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (isRunning) {
                    Button(
                        onClick = { viewModel.cancelSpeedTest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusError.copy(alpha = 0.15f),
                            contentColor = StatusError
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusError)
                    ) {
                        Text(
                            text = "STOP SPEED TEST",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSpeedTest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AirtelRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (state.stage == SpeedTestStage.COMPLETED) "RUN SPEED TEST AGAIN" else "START LIVE SPEED TEST",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SpeedTestHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Live Speed Test",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Real-time Bandwidth & Latency Benchmark",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⚡ SPEED",
                color = AirtelRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SpeedometerGauge(
    currentSpeedMbps: Double,
    stage: SpeedTestStage,
    progress: Float
) {
    val animatedSpeed = remember { Animatable(0f) }

    LaunchedEffect(currentSpeedMbps) {
        animatedSpeed.animateTo(
            targetValue = currentSpeedMbps.toFloat(),
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        )
    }

    // Scale mapping: 0 to 100 Mbps mapped across 240 degrees arc
    val maxGaugeSpeed = 100f
    val speedRatio = (animatedSpeed.value / maxGaugeSpeed).coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val tickColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                Canvas(modifier = Modifier.size(230.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val startAngle = 150f
                    val sweepMaxAngle = 240f

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = sweepMaxAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active speed sweep
                    val activeSweep = sweepMaxAngle * speedRatio
                    if (activeSweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to AirtelRed.copy(alpha = 0.7f),
                                0.5f to AirtelRed,
                                1.0f to NeonGreenAccent
                            ),
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Ticks around gauge (0, 10, 25, 50, 75, 100)
                    val tickValues = listOf(0, 10, 25, 50, 75, 100)
                    val radius = size.minDimension / 2f - strokeWidth
                    val center = this.center

                    for (tick in tickValues) {
                        val angleDeg = startAngle + (sweepMaxAngle * (tick.toFloat() / maxGaugeSpeed))
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val tickStart = center + androidx.compose.ui.geometry.Offset(
                            (radius - 12f) * cos(angleRad).toFloat(),
                            (radius - 12f) * sin(angleRad).toFloat()
                        )
                        val tickEnd = center + androidx.compose.ui.geometry.Offset(
                            (radius - 4f) * cos(angleRad).toFloat(),
                            (radius - 4f) * sin(angleRad).toFloat()
                        )
                        drawLine(
                            color = tickColor,
                            start = tickStart,
                            end = tickEnd,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val speedDisplay = if (currentSpeedMbps > 0.0) {
                        String.format(Locale.US, "%.1f", animatedSpeed.value)
                    } else {
                        "0.0"
                    }

                    Text(
                        text = speedDisplay,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Mbps",
                        color = AirtelRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stage status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (stage == SpeedTestStage.IDLE || stage == SpeedTestStage.COMPLETED) {
                                    AirtelRed.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                1.dp,
                                if (stage == SpeedTestStage.IDLE || stage == SpeedTestStage.COMPLETED) {
                                    AirtelRed.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stage.label,
                            color = if (stage == SpeedTestStage.IDLE || stage == SpeedTestStage.COMPLETED) {
                                AirtelRed
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedHistorySparkline(
    history: List<Float>,
    stage: SpeedTestStage
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val lineColor = if (stage == SpeedTestStage.UPLOAD) StatusInfo else AirtelRed

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (history.size < 2) return@Canvas

            val maxVal = max(history.maxOrNull() ?: 1f, 1f)
            val stepX = size.width / (history.size - 1)
            val path = Path()

            history.forEachIndexed { i, speed ->
                val x = i * stepX
                val normalizedY = 1f - (speed / maxVal).coerceIn(0f, 1f)
                val y = normalizedY * (size.height - 8f) + 4f

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SpeedMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    tint: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isActive) tint.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tint)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = tint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HardwareContextCard(
    modelName: String,
    band: String,
    rsrp: String,
    operator: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CELLULAR BACKHAUL",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$modelName • $operator",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$band • $rsrp",
                    color = AirtelRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
