package com.example.airteldatamonitor.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.SignalMetrics
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusOnlineMuted
import com.example.airteldatamonitor.theme.StatusWarning
import com.example.airteldatamonitor.ui.common.tactileClickable
import java.util.Locale

@Composable
fun AntennaAlignmentScreen(
    viewModel: AntennaAlignmentViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

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
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .tactileClickable(scaleDown = 0.96f, onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Antenna Alignment",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Outdoor Unit (ODU) Signal Tuner",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                .tactileClickable(scaleDown = 0.96f, onClick = onToggleAuditing)
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
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
    val animatedRsrp = remember { Animatable(0f) }
    val animatedSinr = remember { Animatable(0f) }

    // RSRP ranges from -130 dBm (worst) to -60 dBm (best)
    val normalizedRsrpSweep = ((signal.rsrpDbm - (-130)) / 70f).coerceIn(0.05f, 1f)
    val normalizedSinrSweep = (signal.sinrDb / 25.0).toFloat().coerceIn(0.05f, 1f)

    LaunchedEffect(normalizedRsrpSweep) {
        animatedRsrp.animateTo(
            targetValue = normalizedRsrpSweep,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(normalizedSinrSweep) {
        animatedSinr.animateTo(
            targetValue = normalizedSinrSweep,
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

    val sinrText = if (signal.sinrDb >= 0.0) {
        String.format(Locale.US, "+%.1f dB", signal.sinrDb)
    } else {
        String.format(Locale.US, "%.1f dB", signal.sinrDb)
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Dial Canvas
            Box(
                modifier = Modifier.size(236.dp),
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
                        sweepAngle = 260f * animatedRsrp.value,
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
                    drawArc(
                        color = sinrColor,
                        startAngle = 140f,
                        sweepAngle = 260f * animatedSinr.value,
                        useCenter = false,
                        topLeft = Offset(innerPad, innerPad),
                        size = innerSize,
                        style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Dial Center Metrics Display with generous margin from inner arc
                Column(
                    modifier = Modifier.padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RSRP POWER",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${signal.rsrpDbm}",
                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "dBm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "SINR $sinrText",
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                        color = StatusInfo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact Arc Legend & Signal Bars outside dial center
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(qualityColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "RSRP Power",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(StatusInfo)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "SINR Clarity",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "• ${signal.bars}/5 Bars",
                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actionable Alignment Directive
            Text(
                text = directive,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
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
            Row(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isEnabled) AirtelRedMuted else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        contentDescription = "Audio Tone",
                        tint = if (isEnabled) AirtelRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rooftop Audio Pitch Tone",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Emits pitch-rising beeps as signal peaks so you can aim the mast hands-free.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
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
    val peakSinrText = if (peakSinr >= 0.0) {
        String.format(Locale.US, "+%.1f dB", peakSinr)
    } else {
        String.format(Locale.US, "%.1f dB", peakSinr)
    }

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
                Text(
                    text = "SIGNAL TREND (30s)",
                    style = DataTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AirtelRedMuted)
                        .tactileClickable(scaleDown = 0.96f, onClick = onResetPeak)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Reset",
                        color = AirtelRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Column Summary Stat Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.5.sp
                    )
                    Text(
                        text = "$currentRsrp dBm",
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PEAK RSRP",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.5.sp
                    )
                    Text(
                        text = "$peakRsrp dBm",
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                        color = StatusOnline,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PEAK SINR",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.5.sp
                    )
                    Text(
                        text = peakSinrText,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                        color = StatusInfo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart (Rolling 30 seconds)
            if (history.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rotating antenna... gathering telemetry points",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            } else {
                val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padY = 6.dp.toPx()
                    val usableH = h - padY * 2
                    val points = history
                    val minVal = -120f
                    val maxVal = -65f
                    val stepX = w / (points.size - 1).coerceAtLeast(1)

                    // Subtle horizontal guide lines
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, h * 0.33f),
                        end = Offset(w, h * 0.33f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, h * 0.67f),
                        end = Offset(w, h * 0.67f),
                        strokeWidth = 1.dp.toPx()
                    )

                    val strokePath = Path()
                    val fillPath = Path()
                    var lastX = 0f
                    var lastY = h

                    points.forEachIndexed { idx, item ->
                        val normalizedY = ((item.rsrpDbm - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val x = idx * stepX
                        val y = padY + usableH * (1f - normalizedY)

                        if (idx == 0) {
                            strokePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            strokePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                        lastX = x
                        lastY = y
                    }

                    fillPath.lineTo(lastX, h)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(StatusOnline.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = StatusOnline,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    drawCircle(
                        color = StatusOnline,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TowerMetricTile(
                title = "CARRIER BAND",
                value = primaryBand,
                subtitle = signal.frequencyMhz,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            TowerMetricTile(
                title = "CELL ID",
                value = signal.cellId,
                subtitle = "Sector PCI: ${signal.pci}",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TowerMetricTile(
                title = "SIGNAL QUALITY",
                value = "${signal.rsrqDb} dB",
                subtitle = "Reference Quality (RSRQ)",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            TowerMetricTile(
                title = "ODU HARDWARE",
                value = "ZLT W154R+",
                subtitle = "Outdoor Unit Modem",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
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
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Actionable Antenna Installation Best Practices
 */
@Composable
fun AntennaTipsCard() {
    val tips = listOf(
        "Turn in 15° increments: Rotate the outdoor unit slightly, then hold still for 3 seconds to let RF counters settle.",
        "Maximize SINR: High SINR (> 15 dB) guarantees faster speeds than raw RSRP power with heavy interference.",
        "Clear line-of-sight: Keep the front face free of metal siding, eaves, or dense wet foliage."
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates,
                    contentDescription = "Alignment Tips",
                    tint = AirtelRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Professional Alignment Tips",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            tips.forEachIndexed { idx, tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AirtelRedMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                            color = AirtelRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
