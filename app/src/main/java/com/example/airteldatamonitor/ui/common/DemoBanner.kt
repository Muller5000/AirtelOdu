package com.example.airteldatamonitor.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.theme.DemoBadgeBg
import com.example.airteldatamonitor.theme.DemoBadgeBorder
import com.example.airteldatamonitor.theme.DemoBadgeText
import com.example.airteldatamonitor.theme.NeonGreenAccent
import com.example.airteldatamonitor.theme.NeonGreenGlow

@Composable
fun DemoDataBanner(
    modifier: Modifier = Modifier,
    customMessage: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val isLive = customMessage?.contains("LIVE", ignoreCase = true) == true

    val bgColor = if (isLive) NeonGreenGlow else DemoBadgeBg
    val borderColor = if (isLive) NeonGreenAccent.copy(alpha = 0.5f) else DemoBadgeBorder
    val textColor = if (isLive) NeonGreenAccent else DemoBadgeText

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = customMessage ?: "DEMO DATA • Airtel ZLT W154R Plus (Simulated)",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "INFO",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (isLive) "Airtel ZLT W154R Plus Live Mode" else "Demo Notice",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isLive) {
                        "This app is configured to talk directly to your Airtel ZLT W154R Plus router at /cgi-bin/http.cgi.\n\n" +
                            "• Real Telemetry: Signal strength (RSRP, SINR, Band, Cell ID), router uptime, connected hostnames & IPs, and aggregate WAN throughput.\n\n" +
                            "• Truth in Telemetry: The router firmware does not expose per-client flow counters (its internal flow field is 0). Per-device speeds are reported as N/A without fabrication."
                    } else {
                        "All readings, traffic speeds, device metrics, and charts displayed in this version are realistic sample data modeled after the Airtel ZLT W154R Plus router.\n\n" +
                            "You can toggle to Live Router Mode in Settings anytime to connect directly to your router over Wi-Fi."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Understood", color = NeonGreenAccent)
                }
            }
        )
    }
}
