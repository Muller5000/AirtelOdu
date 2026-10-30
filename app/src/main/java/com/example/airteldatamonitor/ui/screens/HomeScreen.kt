package com.example.airteldatamonitor.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.Throughput
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusOnlineMuted
import com.example.airteldatamonitor.ui.common.EmptyStateView
import com.example.airteldatamonitor.ui.common.ErrorStateView
import com.example.airteldatamonitor.ui.common.LoadingStateView
import com.example.airteldatamonitor.ui.common.tactileClickable
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDevices: () -> Unit,
    onNavigateToSpeedTest: () -> Unit = {},
    onNavigateToSmsCenter: () -> Unit = {},
    onNavigateToBandLock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var showEditPlanDialog by remember { mutableStateOf(false) }

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
            val isLive = settings.isLiveRouterMode
            val currentState = state

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Demo switcher (shown only when in simulation mode)
            if (!isLive) {
                item {
                    SimulationStateSwitcher(
                        onSelectMode = { mode -> viewModel.switchSimulationMode(mode) }
                    )
                }
            }

            when (currentState) {
                is UiState.Loading -> {
                    item {
                        LoadingStateView(message = "Reading Airtel ZLT W154R Plus telemetry...")
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorStateView(
                            message = currentState.message,
                            onRetry = { viewModel.loadData() }
                        )
                    }
                }
                is UiState.Empty -> {
                    item {
                        EmptyStateView(
                            title = "No Router Telemetry Recorded",
                            description = "The router reported no active data plan or connected devices for this cycle.",
                            actionLabel = "Refresh Status",
                            onActionClick = { viewModel.loadData() }
                        )
                    }
                }
                is UiState.Success -> {
                    val data = currentState.data
                    val signalSnippet = run {
                        val bandClean = data.routerInfo.primaryBand.replace("Band ", "B")
                        "$bandClean • ${data.routerInfo.signal.rsrpDbm} dBm"
                    }

                    // 1. Single Unified Hero Card (Merges Header + Data Usage + Plan Edit)
                    item {
                        UnifiedHeroCard(
                            dataUsage = data.dataUsage,
                            routerModel = data.routerInfo.modelName,
                            signalSnippet = signalSnippet,
                            isLive = isLive,
                            onEditPlan = { showEditPlanDialog = true }
                        )
                    }

                    // 2. Compact 52dp Live Telemetry Strip
                    item {
                        LiveTelemetryStrip(
                            throughput = data.throughput,
                            uptimeFormatted = data.routerInfo.uptimeFormatted
                        )
                    }

                    // 3. Router Quick Action Tiles (SMS Center & Band Lock)
                    item {
                        QuickActionTiles(
                            onNavigateToSmsCenter = onNavigateToSmsCenter,
                            onNavigateToBandLock = onNavigateToBandLock
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showEditPlanDialog) {
        val currentUsage = (state as? UiState.Success)?.data?.dataUsage
        EditPlanDialog(
            initialName = currentUsage?.planName ?: settings.planName,
            initialLimitGb = currentUsage?.totalPlanGb ?: settings.monthlyPlanGb,
            onDismiss = { showEditPlanDialog = false },
            onSave = { name, limit ->
                viewModel.updateDataPlan(name, limit)
                showEditPlanDialog = false
            }
        )
    }
}

/**
 * Single Unified Hero Card combining:
 * - Brand badge & Gateway hardware status
 * - Live cellular tower band & RSRP
 * - Tabular hero remaining data metric
 * - Animated progress track
 * - Sub-metrics & quick edit pencil
 */
@Composable
fun UnifiedHeroCard(
    dataUsage: DataUsage,
    routerModel: String,
    signalSnippet: String,
    isLive: Boolean,
    onEditPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetSweep = (dataUsage.usagePercentage / 100f).coerceIn(0f, 1f)

    LaunchedEffect(targetSweep) {
        animatedProgress.animateTo(
            targetValue = targetSweep,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            // Row 1: Brand & Gateway info (Left) + Signal Status Pill & Edit Button (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(AirtelRed)
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "airtel",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = dataUsage.planName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = if (isLive) "ZLT W154R+ • 4G LTE" else "Simulated Gateway",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Signal / Mode Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLive) StatusOnlineMuted else AirtelRedMuted)
                            .border(
                                1.dp,
                                if (isLive) StatusOnline.copy(alpha = 0.4f) else AirtelRed.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isLive) StatusOnline else AirtelRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLive) signalSnippet else "DEMO",
                                color = if (isLive) StatusOnline else AirtelRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Edit Plan Button (Emil Kowalski tactile press)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .tactileClickable(scaleDown = 0.92f, onClick = onEditPlan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✏️",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Hero Tabular Metric & Days Left
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DATA REMAINING",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", dataUsage.remainingGb),
                            style = DataTypography.heroMetric,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "GB",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Days left pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${dataUsage.daysRemainingInCycle} days left",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f%% used", dataUsage.usagePercentage),
                        style = DataTypography.labelCaps,
                        color = AirtelRed,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 3: Progress track
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
                                colors = listOf(AirtelRed, Color(0xFFFF5252))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 4: Summary Footnote (Used vs Total • Daily Average)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "Used: %.1f of %.0f GB", dataUsage.usedGb, dataUsage.totalPlanGb),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
                Text(
                    text = String.format(Locale.US, "Avg: %.1f GB/d", dataUsage.dailyAverageGb),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

/**
 * Compact 52dp Live Telemetry Strip showing Download, Upload, and Latency in a single bar.
 */
@Composable
fun LiveTelemetryStrip(
    throughput: Throughput,
    uptimeFormatted: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Downstream
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "↓",
                    color = StatusOnline,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f Mbps", throughput.downloadSpeedMbps),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Peak ${throughput.peakDownloadTodayMbps.toInt()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.5.sp
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Upstream
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "↑",
                    color = StatusInfo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f Mbps", throughput.uploadSpeedMbps),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Upload",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.5.sp
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Ping & Uptime
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(
                        text = "${throughput.pingMs} ms",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Up $uptimeFormatted",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

/**
 * 2 Non-Redundant Quick Action Tiles (SIM & SMS Center + Carrier Band Lock).
 * Removed Devices and Speed Test to avoid duplicating the bottom navigation dock.
 */
@Composable
fun QuickActionTiles(
    onNavigateToSmsCenter: () -> Unit,
    onNavigateToBandLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickTile(
            icon = "📩",
            iconTint = Color(0xFFF59E0B),
            title = "SIM & SMS",
            value = "SMS Center",
            subtitle = "Airtime, OTP & 141",
            onClick = onNavigateToSmsCenter,
            modifier = Modifier.weight(1f)
        )

        QuickTile(
            icon = "📶",
            iconTint = StatusOnline,
            title = "Band Lock",
            value = "Carrier Bands",
            subtitle = "LTE B1, B3, B7, B20",
            onClick = onNavigateToBandLock,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickTile(
    icon: String,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .tactileClickable(scaleDown = 0.96f, onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "→",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title.uppercase(),
                style = DataTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(1.dp))

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
 * Edit Plan Dialog allowing custom allowance entry or one-tap presets.
 */
@Composable
fun EditPlanDialog(
    initialName: String,
    initialLimitGb: Double,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var planName by remember { mutableStateOf(initialName) }
    var limitInput by remember { mutableStateOf(initialLimitGb.toInt().toString()) }
    val presets = listOf(30, 50, 75, 100, 150, 250, 500)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configure Data Plan",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Set your custom Airtel data allowance. The app will calculate usage against this limit.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AirtelRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Limit (GB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AirtelRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Text(
                    text = "Quick Presets:",
                    style = DataTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { gb ->
                        val isSelected = limitInput == gb.toString()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AirtelRed else MaterialTheme.colorScheme.surfaceVariant)
                                .tactileClickable(scaleDown = 0.92f) {
                                    limitInput = gb.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$gb GB",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitInput.toDoubleOrNull() ?: 150.0
                    onSave(planName, limit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AirtelRed)
            ) {
                Text("Save Plan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SimulationStateSwitcher(
    onSelectMode: (DemoSimulationMode) -> Unit
) {
    Column {
        Text(
            text = "DEMO SIMULATION CONTROLS",
            style = DataTypography.labelCaps,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DemoSimulationMode.entries) { mode ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .tactileClickable(scaleDown = 0.94f) { onSelectMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
