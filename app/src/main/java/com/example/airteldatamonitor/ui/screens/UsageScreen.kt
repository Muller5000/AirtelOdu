package com.example.airteldatamonitor.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.model.UsagePeriod
import com.example.airteldatamonitor.theme.AirtelRed
import com.example.airteldatamonitor.theme.AirtelRedMuted
import com.example.airteldatamonitor.theme.DataTypography
import com.example.airteldatamonitor.theme.StatusOnline
import com.example.airteldatamonitor.theme.StatusWarning
import com.example.airteldatamonitor.ui.common.CategoryBreakdownView
import com.example.airteldatamonitor.ui.common.EmptyStateView
import com.example.airteldatamonitor.ui.common.ErrorStateView
import com.example.airteldatamonitor.ui.common.LoadingStateView
import com.example.airteldatamonitor.ui.common.MetricCard
import com.example.airteldatamonitor.ui.common.UsageBarChart
import com.example.airteldatamonitor.ui.common.tactileClickable
import java.util.Locale

@Composable
fun UsageScreen(
    viewModel: UsageViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val bgColor = MaterialTheme.colorScheme.background
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                UsageHeader(isLive = settings.isLiveRouterMode)
            }

            // Period Selector Tabs (Daily, Weekly, Monthly)
            item {
                PeriodSelectorTabs(
                    selectedPeriod = selectedPeriod,
                    onSelect = { viewModel.selectPeriod(it) }
                )
            }

            when (val currentState = state) {
                is UiState.Loading -> {
                    item {
                        LoadingStateView(message = "Aggregating ${selectedPeriod.label} traffic history...")
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorStateView(
                            message = currentState.message,
                            onRetry = { viewModel.loadHistory() }
                        )
                    }
                }
                is UiState.Empty -> {
                    item {
                        EmptyStateView(
                            title = "No Traffic History Found",
                            description = "No historical usage data points are available for this interval.",
                            actionLabel = "Reload Usage",
                            onActionClick = { viewModel.loadHistory() }
                        )
                    }
                }
                is UiState.Success -> {
                    val history = currentState.data

                    // Summary KPI row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Period Total",
                                value = String.format(Locale.US, "%.1f GB", history.totalGb),
                                subtitle = "Average: ${String.format(Locale.US, "%.1f GB", history.averageGb)}",
                                iconSymbol = "Σ",
                                iconTint = AirtelRed,
                                modifier = Modifier.weight(1f)
                            )

                            MetricCard(
                                title = "Peak Day",
                                value = String.format(Locale.US, "%.1f GB", history.peakGb),
                                subtitle = history.peakLabel,
                                iconSymbol = "▲",
                                iconTint = StatusWarning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Bar Chart
                    item {
                        UsageBarChart(dataPoints = history.dataPoints)
                    }

                    // Activity Breakdown
                    if (history.categories.isNotEmpty()) {
                        item {
                            CategoryBreakdownView(categories = history.categories)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UsageHeader(isLive: Boolean) {
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
                text = "Usage Analytics",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = if (isLive) "ZLT W154R+ • Current Cycle WAN Counters" else "Modeled Router Traffic Trends",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AirtelRedMuted)
                .border(1.dp, AirtelRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(
                text = "HISTORY",
                color = AirtelRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PeriodSelectorTabs(
    selectedPeriod: UsagePeriod,
    onSelect: (UsagePeriod) -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        UsagePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) AirtelRedMuted else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) AirtelRed.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .tactileClickable(scaleDown = 0.94f) { onSelect(period) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (period) {
                        UsagePeriod.DAILY -> "Daily"
                        UsagePeriod.WEEKLY -> "Weekly"
                        UsagePeriod.MONTHLY -> "Monthly"
                    },
                    color = if (isSelected) AirtelRed else textSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
