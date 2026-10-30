package com.example.airteldatamonitor.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DeviceCategory
import com.example.airteldatamonitor.data.model.DeviceStatus
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.StatusError
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusOnlineMuted
import com.example.airteldatamonitor.theme.StatusWarning
import com.example.airteldatamonitor.ui.common.EmptyStateView
import com.example.airteldatamonitor.ui.common.ErrorStateView
import com.example.airteldatamonitor.ui.common.LoadingStateView
import com.example.airteldatamonitor.ui.common.tactileClickable

@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()
    val isOperating by viewModel.isOperating.collectAsState()

    var deviceToRename by remember { mutableStateOf<ConnectedDevice?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var deviceToBlock by remember { mutableStateOf<ConnectedDevice?>(null) }

    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                DevicesHeader(isLive = settings.isLiveRouterMode)
            }

            // Compact Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by name, IP, or MAC...", color = textSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = AirtelRed,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DeviceFilter.entries) { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) AirtelRedMuted else cardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) AirtelRed else cardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) AirtelRed else textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Feedback Banner
            actionFeedback?.let { feedbackMsg ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AirtelRedMuted)
                            .border(1.dp, AirtelRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = feedbackMsg,
                            color = textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            when (val currentState = state) {
                is UiState.Loading -> {
                    item {
                        LoadingStateView(message = "Scanning connected devices...")
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorStateView(
                            message = currentState.message,
                            onRetry = { viewModel.loadDevices() }
                        )
                    }
                }
                is UiState.Empty -> {
                    item {
                        EmptyStateView(
                            title = "No Connected Devices",
                            description = "No active Wi-Fi or LAN devices were found on the router.",
                            actionLabel = "Rescan Devices",
                            onActionClick = { viewModel.loadDevices() }
                        )
                    }
                }
                is UiState.Success -> {
                    val filteredDevices = viewModel.filterDevices(currentState.data)

                    if (filteredDevices.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Matching Devices",
                                description = "No devices matched the search term \"$searchQuery\".",
                                actionLabel = "Clear Filter",
                                onActionClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setFilter(DeviceFilter.ALL)
                                }
                            )
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONNECTED CLIENTS",
                                    color = textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "${filteredDevices.size} Active",
                                    color = StatusOnline,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        items(filteredDevices, key = { it.id }) { device ->
                            DeviceCard(
                                device = device,
                                isOperating = isOperating == device.macAddress,
                                onRenameClick = { dev ->
                                    deviceToRename = dev
                                    renameInputText = dev.customNickname ?: ""
                                },
                                onBlockToggleClick = { dev ->
                                    if (dev.isBlocked) {
                                        viewModel.toggleBlockDevice(dev)
                                    } else {
                                        deviceToBlock = dev
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    deviceToRename?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToRename = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Customize Device Name",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set a friendly nickname for this device (${dev.macAddress}):",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        placeholder = { Text(dev.name, color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = bgColor,
                            unfocusedContainerColor = bgColor,
                            focusedBorderColor = AirtelRed,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameDevice(dev, renameInputText)
                        deviceToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AirtelRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Name", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (dev.customNickname != null) {
                        Button(
                            onClick = {
                                viewModel.renameDevice(dev, "")
                                deviceToRename = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBorder,
                                contentColor = StatusWarning
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { deviceToRename = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBorder,
                            contentColor = textSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }
        )
    }

    // Block Confirmation Dialog
    deviceToBlock?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToBlock = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Block Device Access?",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to block ${dev.displayName} (${dev.macAddress}) from router access?\n\nThe device's packets will be filtered at the router firewall, terminating its internet connection.",
                    color = textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleBlockDevice(dev)
                        deviceToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusError,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Block Device", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { deviceToBlock = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBorder,
                        contentColor = textSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun DevicesHeader(isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Connected Devices",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = if (isLive) "ZLT W154R+ • Wi-Fi & LAN Clients" else "Simulated Device Network",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLive) StatusOnlineMuted else AirtelRedMuted)
                .border(
                    1.dp,
                    if (isLive) StatusOnline.copy(alpha = 0.4f) else AirtelRed.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isLive) StatusOnline else AirtelRed)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isLive) "LIVE DHCP" else "DEMO",
                    color = if (isLive) StatusOnline else AirtelRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Editorial Compact DeviceCard.
 * Clean, readable, and removes repetitive 3-column N/A* clutter.
 */
@Composable
fun DeviceCard(
    device: ConnectedDevice,
    isOperating: Boolean = false,
    onRenameClick: (ConnectedDevice) -> Unit = {},
    onBlockToggleClick: (ConnectedDevice) -> Unit = {}
) {
    val categoryIcon = when (device.category) {
        DeviceCategory.SMARTPHONE -> "📱"
        DeviceCategory.LAPTOP -> "💻"
        DeviceCategory.SMART_TV -> "📺"
        DeviceCategory.GAMING_CONSOLE -> "🎮"
        DeviceCategory.TABLET -> "📱"
        DeviceCategory.SMART_SPEAKER -> "🔊"
        DeviceCategory.IOT_DEVICE -> "💡"
        DeviceCategory.WORKSTATION -> "🖥"
    }

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (device.isBlocked) StatusError.copy(alpha = 0.5f) else cardBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Concentric Squircle Icon: R_inner = 10.dp (R_outer = 18.dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (device.isBlocked) StatusError.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (device.isBlocked) "🚫" else categoryIcon,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.displayName,
                        color = if (device.isBlocked) StatusError else textPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (device.isBlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusError.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("BLOCKED", color = StatusError, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${device.ipAddress} • ${device.connectionType.label}",
                    color = textSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )

                if (device.customNickname != null) {
                    Text(
                        text = "${device.name} • ${device.macAddress}",
                        color = textSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Rename Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .tactileClickable(scaleDown = 0.92f) { onRenameClick(device) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Rename",
                        tint = textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Block/Allow Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (device.isBlocked) StatusOnline.copy(alpha = 0.12f) else StatusError.copy(alpha = 0.12f))
                        .tactileClickable(scaleDown = 0.92f, enabled = !isOperating) { onBlockToggleClick(device) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isOperating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = if (device.isBlocked) StatusOnline else StatusError,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (device.isBlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Block,
                            contentDescription = if (device.isBlocked) "Unblock" else "Block",
                            tint = if (device.isBlocked) StatusOnline else StatusError,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
