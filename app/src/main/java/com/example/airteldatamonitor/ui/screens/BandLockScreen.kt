package com.example.airteldatamonitor.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.BandLockState
import com.example.airteldatamonitor.data.model.BandOption
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandLockScreen(
    viewModel: BandLockViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val uiState by viewModel.uiState.collectAsState()
    val is4gLockEnabled by viewModel.is4gLockEnabled.collectAsState()
    val is5gLockEnabled by viewModel.is5gLockEnabled.collectAsState()
    val selected4gBands by viewModel.selected4gBands.collectAsState()
    val selected5gBands by viewModel.selected5gBands.collectAsState()
    val isApplying by viewModel.isApplying.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Band Locking Tool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "LTE & 5G Carrier Frequency Lock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadBandLockState() },
                        enabled = !isApplying
                    ) {
                        Text(
                            text = "↻",
                            fontSize = 22.sp,
                            color = StatusInfo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = !isApplying,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AirtelRed,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isApplying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Committing to Modem...", fontWeight = FontWeight.Bold)
                        } else {
                            Text("🔒", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply Band Lock", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Feedback notification banner
            AnimatedVisibility(
                visible = actionFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                actionFeedback?.let { feedback ->
                    val isErr = feedback.startsWith("✗")
                    val bg = if (isErr) StatusError.copy(alpha = 0.15f) else NeonGreenAccent.copy(alpha = 0.15f)
                    val border = if (isErr) StatusError.copy(alpha = 0.5f) else NeonGreenAccent.copy(alpha = 0.5f)
                    val textCol = if (isErr) StatusError else NeonGreenAccent

                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = feedback,
                                color = textCol,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearFeedback() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    color = textCol,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = StatusInfo)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Reading modem RF carrier frequency bands...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("⚠", color = StatusError, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Failed to Read Modem Bands", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadBandLockState() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Retry", color = StatusInfo)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val config = state.data
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Current Connected Band Banner
                        item {
                            ConnectedBandCard(connectedBand = config.currentConnectedBand)
                        }

                        // 2. Quick Presets
                        item {
                            PresetsSection(
                                onSelectPreset = { preset -> viewModel.applyPreset(preset) }
                            )
                        }

                        // 3. 4G Band Locking Card
                        item {
                            BandCategoryCard(
                                title = "4G LTE Bands",
                                subtitle = "Forces modem to aggregate only selected LTE frequencies",
                                isLockEnabled = is4gLockEnabled,
                                onToggleLock = { viewModel.toggle4gLock(it) },
                                bands = config.supported4gBands,
                                selectedBands = selected4gBands,
                                is5G = false,
                                onToggleBand = { band -> viewModel.toggleBand(band, is5G = false) }
                            )
                        }

                        // 4. 5G Band Locking Card
                        item {
                            BandCategoryCard(
                                title = "5G NR Bands",
                                subtitle = "Forces modem into high-capacity 5G carrier frequencies",
                                isLockEnabled = is5gLockEnabled,
                                onToggleLock = { viewModel.toggle5gLock(it) },
                                bands = config.supported5gBands,
                                selectedBands = selected5gBands,
                                is5G = true,
                                onToggleBand = { band -> viewModel.toggleBand(band, is5G = true) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Safety Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ", color = StatusInfo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Band Lock?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "The cellular modem will briefly disconnect and reconnect to lock into the selected frequencies.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• 4G Locking: ${if (is4gLockEnabled) "Enabled (${selected4gBands.size} bands)" else "Auto (All bands)"}",
                        color = StatusInfo,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• 5G Locking: ${if (is5gLockEnabled) "Enabled (${selected5gBands.size} bands)" else "Auto (All bands)"}",
                        color = NeonGreenAccent,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.applyChanges()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AirtelRed, contentColor = Color.White)
                ) {
                    Text("Confirm & Lock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ConnectedBandCard(connectedBand: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(StatusInfo.copy(alpha = 0.15f))
                    .border(1.dp, StatusInfo.copy(alpha = 0.4f), CircleShape)
            ) {
                Text("📶", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACTIVE CARRIER FREQUENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = connectedBand.ifEmpty { "Detecting..." },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                color = NeonGreenAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreenAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "LIVE",
                    color = NeonGreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PresetsSection(
    onSelectPreset: (BandPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "OPTIMIZATION PRESETS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PresetButton(
                label = "⚡ Max Speed",
                description = "B3+B7+n78",
                tint = StatusInfo,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset(BandPreset.MAX_SPEED) }
            )
            PresetButton(
                label = "🧱 Indoors",
                description = "B20+B28",
                tint = StatusWarning,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset(BandPreset.STRONGEST_INDOORS) }
            )
            PresetButton(
                label = "🔄 Auto All",
                description = "Unlock all",
                tint = NeonGreenAccent,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset(BandPreset.AUTO_ALL_BANDS) }
            )
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    description: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun BandCategoryCard(
    title: String,
    subtitle: String,
    isLockEnabled: Boolean,
    onToggleLock: (Boolean) -> Unit,
    bands: List<BandOption>,
    selectedBands: Set<Int>,
    is5G: Boolean,
    onToggleBand: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isLockEnabled) (if (is5G) AirtelRed.copy(alpha = 0.5f) else StatusInfo.copy(alpha = 0.5f))
                else MaterialTheme.colorScheme.outline
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isLockEnabled) {
                            Surface(
                                color = (if (is5G) AirtelRed else StatusInfo).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LOCKED (${selectedBands.size})",
                                    color = if (is5G) AirtelRed else StatusInfo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isLockEnabled,
                    onCheckedChange = onToggleLock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (is5G) AirtelRed else StatusInfo,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Band selection chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bands.forEach { band ->
                    val isChecked = selectedBands.contains(band.bandNumber)
                    BandSelectableItem(
                        band = band,
                        isSelected = isChecked,
                        isEnabled = isLockEnabled,
                        is5G = is5G,
                        onClick = {
                            if (isLockEnabled) {
                                onToggleBand(band.bandNumber)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BandSelectableItem(
    band: BandOption,
    isSelected: Boolean,
    isEnabled: Boolean,
    is5G: Boolean,
    onClick: () -> Unit
) {
    val activeColor = if (is5G) AirtelRed else StatusInfo
    val borderColor = when {
        !isEnabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        isSelected -> activeColor.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.outline
    }
    val bgColor = when {
        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        isSelected -> activeColor.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected && isEnabled) activeColor else Color.Transparent
                    )
                    .border(
                        1.5.dp,
                        if (isSelected && isEnabled) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
            ) {
                if (isSelected && isEnabled) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = band.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = band.frequencyLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) (if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
