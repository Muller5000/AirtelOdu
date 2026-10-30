package com.example.airteldatamonitor.ui.navigation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.repository.RouterRepository
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.ui.common.tactileClickable
import com.example.airteldatamonitor.ui.screens.BandLockScreen
import com.example.airteldatamonitor.ui.screens.BandLockViewModel
import com.example.airteldatamonitor.ui.screens.DevicesScreen
import com.example.airteldatamonitor.ui.screens.DevicesViewModel
import com.example.airteldatamonitor.ui.screens.HomeScreen
import com.example.airteldatamonitor.ui.screens.HomeViewModel
import com.example.airteldatamonitor.ui.screens.SettingsScreen
import com.example.airteldatamonitor.ui.screens.SettingsViewModel
import com.example.airteldatamonitor.ui.screens.SmsCenterScreen
import com.example.airteldatamonitor.ui.screens.SmsCenterViewModel
import com.example.airteldatamonitor.ui.screens.SpeedTestScreen
import com.example.airteldatamonitor.ui.screens.SpeedTestViewModel
import com.example.airteldatamonitor.ui.screens.UsageScreen
import com.example.airteldatamonitor.ui.screens.UsageViewModel

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    SPEED("Speed", Icons.Rounded.Speed),
    DEVICES("Devices", Icons.Rounded.Devices),
    USAGE("Usage", Icons.Rounded.DataUsage),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun MainAppScreen(
    repository: RouterRepository = remember { RouterRepository() }
) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var isBandLockVisible by remember { mutableStateOf(false) }
    var isSmsCenterVisible by remember { mutableStateOf(false) }

    val homeViewModel = remember(repository) { HomeViewModel(repository) }
    val speedTestViewModel = remember(repository) { SpeedTestViewModel(repository) }
    val devicesViewModel = remember(repository) { DevicesViewModel(repository) }
    val usageViewModel = remember(repository) { UsageViewModel(repository) }
    val settingsViewModel = remember(repository) { SettingsViewModel(repository) }
    val bandLockViewModel = remember(repository) { BandLockViewModel(repository) }
    val smsCenterViewModel = remember(repository) { SmsCenterViewModel(repository) }

    val bgColor = MaterialTheme.colorScheme.background

    if (isSmsCenterVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            SmsCenterScreen(
                viewModel = smsCenterViewModel,
                onNavigateBack = { isSmsCenterVisible = false }
            )
        }
    } else if (isBandLockVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            BandLockScreen(
                viewModel = bandLockViewModel,
                onNavigateBack = { isBandLockVisible = false }
            )
        }
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
            containerColor = bgColor,
            bottomBar = {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onSelectDestination = { currentDestination = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestination.HOME -> HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToDevices = { currentDestination = AppDestination.DEVICES },
                        onNavigateToSpeedTest = { currentDestination = AppDestination.SPEED },
                        onNavigateToSmsCenter = { isSmsCenterVisible = true },
                        onNavigateToBandLock = { isBandLockVisible = true }
                    )
                    AppDestination.SPEED -> SpeedTestScreen(
                        viewModel = speedTestViewModel
                    )
                    AppDestination.DEVICES -> DevicesScreen(
                        viewModel = devicesViewModel
                    )
                    AppDestination.USAGE -> UsageScreen(
                        viewModel = usageViewModel
                    )
                    AppDestination.SETTINGS -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToBandLock = { isBandLockVisible = true },
                        onNavigateToSmsCenter = { isSmsCenterVisible = true }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentDestination: AppDestination,
    onSelectDestination: (AppDestination) -> Unit
) {
    val navSurface = MaterialTheme.colorScheme.surface
    val navOutline = MaterialTheme.colorScheme.outline
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(navSurface)
            .border(1.dp, navOutline, RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination
                val iconColor = if (isSelected) AirtelRed else inactiveColor
                val textColor = if (isSelected) AirtelRed else inactiveColor

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AirtelRedMuted else Color.Transparent)
                        .tactileClickable(scaleDown = 0.94f) { onSelectDestination(destination) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = destination.label,
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
