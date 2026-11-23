package com.example.airteldatamonitor.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.SignalMetrics
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.NeonGreenAccent
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusOnlineMuted
import com.example.airteldatamonitor.theme.StatusWarning
import com.example.airteldatamonitor.ui.common.tactileClickable
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AntennaAlignmentScreen(
    viewModel: AntennaAlignmentViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signal by viewModel.currentSignal.collectAsState()
    val primaryBand by viewModel.primaryBand.collectAsState()
    val modelName by viewModel.modelName.collectAsState()
    val peakRsrp by viewModel.peakRsrpDbm.collectAsState()
    val peakSinr by viewModel.peakSinrDb.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()
    val isAudioToneEnabled by viewModel.isAudioToneEnabled.collectAsState()
    val history by viewModel.historySamples.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.startPolling()
        onDispose {
            viewModel.stopPolling()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                AntennaAlignmentHeader(
                    isAuditing = isAuditing,
                    onToggleAuditing = { viewModel.toggleAuditing() },
                    onNavigateBack = onNavigateBack
                )
            }

            // 1. Hero RF Circular Gauge
            item {
                RfAlignmentHeroGauge(
                    signal = signal,
                    primaryBand = primaryBand
                )
            }

            // 2. Ladder / Roof Audio Tone Toggle
            item {
                LadderAudioToneCard(
                    isEnabled = isAudioToneEnabled,
                    onToggle = { viewModel.toggleAudioTone(it) }
                )
            }

            // 3. Real-Time Signal History Sparkline & Peak tracker
            item {
                SignalTrendCard(
                    history = history,
                    currentRsrp = signal.rsrpDbm,
                    peakRsrp = peakRsrp,
                    peakSinr = peakSinr,
                    onResetPeak = { viewModel.resetPeaks() }
                )
            }

            // 4. Cellular Tower Identity Grid
            item {
                TowerIdentityGrid(
                    signal = signal,
                    primaryBand = primaryBand,
                    modelName = modelName
                )
            }

            // 5. Antenna Installation Guidelines
            item {
                AntennaTipsCard()
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AntennaAlignmentHeader(
    isAuditing: Boolean,
    onToggleAuditing: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .tactileClickable(scaleDown = 0.92f, onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Antenna Alignment",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Outdoor Unit (ODU) Signal Tuner",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live / Paused Audit Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isAuditing) StatusOnlineMuted else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (isAuditing) StatusOnline.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
                .tactileClickable(scaleDown = 0.94f, onClick = onToggleAuditing)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isAuditing) StatusOnline else MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isAuditing) "LIVE (1s)" else "PAUSED",
                    color = if (isAuditing) StatusOnline else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Hero RF Circular Gauge with Dual Arcs:
 * - Outer Arc: RSRP Power (Signal strength)
 * - Inner Arc: SINR Quality (Signal-to-Interference-Noise ratio)
 */
@Composable
fun RfAlignmentHeroGauge(
    signal: SignalMetrics,
    primaryBand: String
) {
    val animatedProgress = remember { Animatable(0f) }
    // RSRP ranges from -130 dBm (worst) to -60 dBm (best)
    val normalizedSweep = ((signal.rsrpDbm - (-130)) / 70f).coerceIn(0.05f, 1f)

    LaunchedEffect(normalizedSweep) {
        animatedProgress.animateTo(
            targetValue = normalizedSweep,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    val (qualityLabel, qualityColor, directive) = when {
        signal.rsrpDbm >= -80 && signal.sinrDb >= 13.0 -> Triple(
            "OPTIMAL ALIGNMENT",
            StatusOnline,
            "Target mast acquired with low interference. Lock bracket bolts."
        )
        signal.rsrpDbm >= -92 -> Triple(
            "GOOD RECEPTION",
            StatusInfo,
            "Stable connection. Pan antenna by 5° to peak SINR quality."
        )
        signal.rsrpDbm >= -105 -> Triple(
            "FAIR SIGNAL",
            StatusWarning,
            "Higher tower interference. Clear direct line of sight or elevate pole."
        )
        else -> Triple(
            "WEAK SIGNAL",
            AirtelRed,
            "Searching for carrier signal. Rotate toward nearest Airtel mast."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Signal Quality Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(qualityColor.copy(alpha = 0.12f))
                    .border(1.dp, qualityColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = qualityLabel,
                    color = qualityColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Dial Canvas
            Box(
                modifier = Modifier
                    .size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val rsrpColor = qualityColor
                val sinrColor = StatusInfo

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val innerStrokeWidth = 8.dp.toPx()
                    val arcPadding = strokeWidth / 2f
                    val sizeArc = Size(size.width - strokeWidth, size.height - strokeWidth)

                    // Outer Track (RSRP)
                    drawArc(
                        color = trackColor,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = sizeArc,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Outer Active Arc (RSRP)
                    drawArc(
                        color = rsrpColor,
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress.value,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = sizeArc,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Inner Concentric Track (SINR)
                    val innerPad = strokeWidth + 12.dp.toPx()
                    val innerSize = Size(size.width - innerPad * 2, size.height - innerPad * 2)
                    drawArc(
                        color = trackColor.copy(alpha = 0.6f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(innerPad, innerPad),
                        size = innerSize,
                        style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                    )

                    // Inner Active Arc (SINR)
                    val sinrNormalized = (signal.sinrDb / 25.0).toFloat().coerceIn(0.05f, 1f)
                    drawArc(
                        color = sinrColor,
                        startAngle = 140f,
                        sweepAngle = 260f * sinrNormalized,
                        useCenter = false,
                        topLeft = Offset(innerPad, innerPad),
                        size = innerSize,
                        style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Dial Center Metrics Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RSRP POWER",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${signal.rsrpDbm}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "dBm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Secondary SINR & Quality tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "SINR: +%.1f dB", signal.sinrDb),
                            color = StatusInfo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${signal.bars}/5 Bars",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actionable Alignment Directive
            Text(
                text = directive,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Ladder & Rooftop Audio Tone Feature:
 * Beeps at higher frequency & pitch when signal improves so user can adjust hands-free.
 */
@Composable
fun LadderAudioToneCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔊", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rooftop Audio Pitch Tone",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Emits tone pulses that rise in pitch as signal peaks. Ideal for adjusting the antenna on a ladder without looking at the screen.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AirtelRed,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

/**
 * Real-time Signal Trend Card plotting the last 30 samples and tracking best peak session.
 */
@Composable
fun SignalTrendCard(
    history: List<SignalHistorySample>,
    currentRsrp: Int,
    peakRsrp: Int,
    peakSinr: Double,
    onResetPeak: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIGNAL HISTORY (LAST 30s)",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Current: $currentRsrp dBm",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusOnlineMuted)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Peak: $peakRsrp dBm",
                            color = StatusOnline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Reset",
                        color = AirtelRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .tactileClickable(scaleDown = 0.92f, onClick = onResetPeak)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart (Rolling 30 seconds)
            if (history.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rotating antenna... gathering telemetry points",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val points = history
                    val minVal = -120f
                    val maxVal = -65f
                    val stepX = w / (points.size - 1).coerceAtLeast(1)

                    val path = androidx.compose.ui.graphics.Path()
                    points.forEachIndexed { idx, item ->
                        val normalizedY = ((item.rsrpDbm - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val x = idx * stepX
                        val y = h - (normalizedY * h)

                        if (idx == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = StatusOnline,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

/**
 * Cellular Tower Identity Grid:
 * Active Carrier Band, Serving Cell ID, RSRQ, and Hardware Model
 */
@Composable
fun TowerIdentityGrid(
    signal: SignalMetrics,
    primaryBand: String,
    modelName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TowerMetricTile(
            title = "CARRIER BAND",
            value = primaryBand,
            subtitle = signal.frequencyMhz,
            modifier = Modifier.weight(1f)
        )

        TowerMetricTile(
            title = "CELL ID",
            value = signal.cellId,
            subtitle = "Sector PCI: ${signal.pci}",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TowerMetricTile(
            title = "SIGNAL QUALITY",
            value = "${signal.rsrqDb} dB",
            subtitle = "Reference Quality (RSRQ)",
            modifier = Modifier.weight(1f)
        )

        TowerMetricTile(
            title = "ODU HARDWARE",
            value = "ZLT W154R+",
            subtitle = "Outdoor Unit Modem",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TowerMetricTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = DataTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
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

/**
 * Actionable Antenna Installation Best Practices
 */
@Composable
fun AntennaTipsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎯", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Professional Alignment Tips",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "1. Turn in 15° increments: Rotate the outdoor antenna slightly, then hold in place for 3 seconds to let RF counters settle.\n" +
                        "2. Maximize SINR: A high SINR (> 15 dB) guarantees significantly faster speeds than high raw power (RSRP) with noise.\n" +
                        "3. Line-of-sight: Ensure the front face of the outdoor unit has an unobstructed view free of metal siding or thick wet trees.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                lineHeight = 17.sp
            )
        }
    }
}
