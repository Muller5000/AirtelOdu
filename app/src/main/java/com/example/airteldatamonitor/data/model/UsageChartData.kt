package com.example.airteldatamonitor.data.model

enum class UsagePeriod(val label: String) {
    DAILY("Daily (24h)"),
    WEEKLY("Weekly (7d)"),
    MONTHLY("Monthly (4w)")
}

data class UsageDataPoint(
    val label: String,
    val downloadGb: Double,
    val uploadGb: Double
) {
    val totalGb: Double get() = downloadGb + uploadGb
}

data class CategoryUsage(
    val categoryName: String,
    val amountGb: Double,
    val percentage: Float,
    val iconName: String = "category"
)

data class UsageHistory(
    val period: UsagePeriod,
    val dataPoints: List<UsageDataPoint>,
    val totalGb: Double,
    val peakLabel: String,
    val peakGb: Double,
    val averageGb: Double,
    val categories: List<CategoryUsage>,
    val isDemoData: Boolean = true
)
