package com.example.airteldatamonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.NetworkMode
import com.example.airteldatamonitor.data.model.RouterConnectionStatus
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.NeonGreenAccent
import com.example.airteldatamonitor.theme.StatusError
import com.example.airteldatamonitor.theme.StatusInfo
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusWarning
import com.example.airteldatamonitor.ui.common.tactileClickable

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToBandLock: () -> Unit = {},
    onNavigateToSmsCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()
    val testFeedback by viewModel.testFeedback.collectAsState()
    val currentNetworkMode by viewModel.currentNetworkMode.collectAsState()
    val isSwitchingNetworkMode by viewModel.isSwitchingNetworkMode.collectAsState()
    val networkSwitchFeedback by viewModel.networkSwitchFeedback.collectAsState()
    val isRebooting by viewModel.isRebooting.collectAsState()
    val rebootCountdown by viewModel.rebootCountdown.collectAsState()
    val rebootFeedback by viewModel.rebootFeedback.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

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
                SettingsHeader()
            }

            // Section 1: Data Plan Allowance (User-Configurable)
            item {
                SettingsSectionTitle("DATA PLAN ALLOWANCE")
            }

            item {
                DataPlanAllowanceCard(
                    planName = settings.planName,
                    monthlyPlanGb = settings.monthlyPlanGb,
                    onUpdatePlan = { name, gb -> viewModel.updateDataPlan(name, gb) }
                )
            }

            // Section 2: Appearance & Theme
            item {
                SettingsSectionTitle("APPEARANCE")
            }

            item {
                AppearanceThemeCard(
                    isDarkMode = settings.isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) }
                )
            }

            // Section 3: Router Connection & Advanced Gateway
            item {
                SettingsSectionTitle("ROUTER CONNECTION & GATEWAY")
            }

            item {
                RouterConnectionCard(
                    settings = settings,
                    status = connectionStatus,
                    isTesting = isTesting,
                    testFeedback = testFeedback,
                    currentNetworkMode = currentNetworkMode,
                    isSwitchingNetworkMode = isSwitchingNetworkMode,
                    networkSwitchFeedback = networkSwitchFeedback,
                    onToggleLiveMode = { viewModel.toggleLiveRouterMode(it) },
                    onTestClick = { viewModel.testConnection() },
                    onIpChange = { viewModel.updateRouterIp(it) },
                    onPortChange = { it.toIntOrNull()?.let { p -> viewModel.updateHttpPort(p) } },
                    onPasswordChange = { viewModel.updatePassword(it) },
                    onSelectNetworkMode = { viewModel.changeNetworkMode(it) },
                    onRefreshIntervalChange = { viewModel.updateRefreshInterval(it) },
                    onLowDataThresholdChange = { viewModel.updateLowDataThreshold(it) },
                    onToggleAlerts = { viewModel.toggleDataAlerts(it) }
                )
            }

            // Section 4: Maintenance
            item {
                SettingsSectionTitle("MAINTENANCE")
            }

            item {
                RouterMaintenanceCard(
                    isRebooting = isRebooting,
                    rebootCountdown = rebootCountdown,
                    feedback = rebootFeedback,
                    onRebootClick = { viewModel.rebootRouter() }
                )
            }

            // Footer: Version & Architecture note dialog button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Airtel ODU Monitor • v2.14.0",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "About Hardware Architecture & Endpoints",
                            color = AirtelRed,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .tactileClickable(scaleDown = 0.95f) { showAboutDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAboutDialog) {
        AboutArchitectureDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SettingsHeader() {
    Column {
        Text(
            text = "Settings",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Data Allowance, Connection & Preferences",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    )
}

/**
 * Data Plan Allowance Card: Allows user to configure their custom monthly data limit
 * using either quick preset pills or a custom dialog.
 */
@Composable
fun DataPlanAllowanceCard(
    planName: String,
    monthlyPlanGb: Double,
    onUpdatePlan: (String, Double) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val presets = listOf(30, 50, 75, 100, 150, 250)

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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Monthly Allowance",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$planName • ${monthlyPlanGb.toInt()} GB Allowance",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AirtelRedMuted)
                        .border(1.dp, AirtelRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${monthlyPlanGb.toInt()} GB",
                        color = AirtelRed,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SELECT PLAN PRESET:",
                style = DataTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Presets row with Custom button at the end
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { gb ->
                    val isSelected = monthlyPlanGb.toInt() == gb
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AirtelRed else MaterialTheme.colorScheme.surfaceVariant)
                            .tactileClickable(scaleDown = 0.93f) {
                                onUpdatePlan(planName, gb.toDouble())
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$gb GB",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, AirtelRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .tactileClickable(scaleDown = 0.93f) { showCustomDialog = true }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Custom...",
                            color = AirtelRed,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        EditPlanDialog(
            initialName = planName,
            initialLimitGb = monthlyPlanGb,
            onDismiss = { showCustomDialog = false },
            onSave = { name, limit ->
                onUpdatePlan(name, limit)
                showCustomDialog = false
            }
        )
    }
}

/**
 * Polished Dual-Pill Appearance Card (Emil Kowalski micro-interaction).
 */
@Composable
fun AppearanceThemeCard(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
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
                Text(
                    text = "App Theme",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDarkMode) "Obsidian Dark Mode (OLED)" else "Editorial Light Mode (Clean Paper)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Dual pill segmented toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) AirtelRed else Color.Transparent)
                        .tactileClickable(scaleDown = 0.92f) { onToggleDarkMode(true) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Dark",
                            color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isDarkMode) AirtelRed else Color.Transparent)
                        .tactileClickable(scaleDown = 0.92f) { onToggleDarkMode(false) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("☀️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Light",
                            color = if (!isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Consolidated Router Connection Card with Progressive Disclosure.
 * Keeps basic Live switch and Test button up front, collapsing technical gateway/radio settings.
 */
@Composable
fun RouterConnectionCard(
    settings: com.example.airteldatamonitor.data.model.RouterSettings,
    status: RouterConnectionStatus,
    isTesting: Boolean,
    testFeedback: String?,
    currentNetworkMode: NetworkMode,
    isSwitchingNetworkMode: Boolean,
    networkSwitchFeedback: String?,
    onToggleLiveMode: (Boolean) -> Unit,
    onTestClick: () -> Unit,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSelectNetworkMode: (NetworkMode) -> Unit,
    onRefreshIntervalChange: (Int) -> Unit,
    onLowDataThresholdChange: (Int) -> Unit,
    onToggleAlerts: (Boolean) -> Unit
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val statusColor = when (status) {
        RouterConnectionStatus.CONNECTED -> NeonGreenAccent
        RouterConnectionStatus.CONNECTING -> StatusInfo
        RouterConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
        RouterConnectionStatus.AUTH_FAILED -> StatusError
        RouterConnectionStatus.UNREACHABLE -> StatusError
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
            // Live Router Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Live Router Mode",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (settings.isLiveRouterMode) {
                            "Queries local ZLT W154R+ over Wi-Fi (${settings.routerIp})"
                        } else {
                            "Simulating realistic Airtel metrics"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = settings.isLiveRouterMode,
                    onCheckedChange = onToggleLiveMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AirtelRed,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Row + Test Connection Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status.label,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onTestClick,
                    enabled = !isTesting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = AirtelRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isTesting) "Probing..." else "Test Connection",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (testFeedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = testFeedback,
                    color = if (testFeedback.startsWith("✓")) StatusOnline else StatusError,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Advanced Gateway & Radio Dropdown (Progressive Disclosure)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .tactileClickable(scaleDown = 0.98f) { isAdvancedExpanded = !isAdvancedExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚙️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Advanced Gateway & Radio Settings",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (isAdvancedExpanded) "▲" else "▼",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            AnimatedVisibility(visible = isAdvancedExpanded) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gateway IP & Port
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = settings.routerIp,
                            onValueChange = onIpChange,
                            label = { Text("Gateway IP") },
                            singleLine = true,
                            modifier = Modifier.weight(0.65f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AirtelRed,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = settings.httpPort.toString(),
                            onValueChange = onPortChange,
                            label = { Text("Port") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.35f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AirtelRed,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // Admin Password
                    OutlinedTextField(
                        value = settings.adminPasswordMasked,
                        onValueChange = onPasswordChange,
                        label = { Text("Admin Web Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = AirtelRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .tactileClickable(scaleDown = 0.92f) { passwordVisible = !passwordVisible }
                                    .padding(8.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AirtelRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Cellular Network Mode Switcher
                    Text(
                        text = "CELLULAR RADIO MODE:",
                        style = DataTypography.labelCaps,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NetworkMode.entries.forEach { mode ->
                            val isSelected = currentNetworkMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AirtelRed else MaterialTheme.colorScheme.surfaceVariant)
                                    .tactileClickable(scaleDown = 0.95f) { onSelectNetworkMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (networkSwitchFeedback != null) {
                        Text(
                            text = networkSwitchFeedback,
                            color = if (networkSwitchFeedback.startsWith("✓")) StatusOnline else StatusError,
                            fontSize = 11.sp
                        )
                    }

                    // Refresh interval slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Refresh Interval",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${settings.refreshIntervalSec}s",
                                color = AirtelRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.refreshIntervalSec.toFloat(),
                            onValueChange = { onRefreshIntervalChange(it.toInt()) },
                            valueRange = 5f..60f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = AirtelRed,
                                activeTrackColor = AirtelRed,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Router Maintenance Card: Reboot router with confirmation dialog.
 */
@Composable
fun RouterMaintenanceCard(
    isRebooting: Boolean,
    rebootCountdown: Int,
    feedback: String?,
    onRebootClick: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Restart Gateway",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reboots the ZLT W154R+ modem to refresh carrier connections",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = !isRebooting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AirtelRedMuted,
                        contentColor = AirtelRed
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRebooting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = AirtelRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${rebootCountdown}s", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Reboot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (feedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback,
                    color = if (feedback.startsWith("✓")) StatusOnline else AirtelRed,
                    fontSize = 11.5.sp
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Restart Router?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will temporarily disconnect all connected Wi-Fi devices for approximately 60 seconds while the router power cycles.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onRebootClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AirtelRed)
                ) {
                    Text("Reboot Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * About Architecture Dialog replacing the massive static card.
 */
@Composable
fun AboutArchitectureDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ZLT W154R+ Architecture",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Direct HTTP Gateway Integration",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = AirtelRed
                )
                Text(
                    text = "This application communicates directly with the Airtel ZLT W154R Plus ODU router over local Wi-Fi / Ethernet without requiring external cloud servers.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Key Endpoints Utilized:",
                    style = DataTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• /goform/goform_get_cmd_process (Network Info, Traffic, Stations)\n• /xml/default_ap_station_info.xml (DHCP connected devices)\n• /goform/set_band_lock_action (Carrier frequency management)\n• /goform/goform_set_cmd_process (Reboot & Radio mode)",
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AirtelRed)
            ) {
                Text("Got it", color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
