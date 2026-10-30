package com.example.airteldatamonitor.data.model

data class DataUsage(
    val planName: String = "Airtel Ultra Unlimited 150GB",
    val totalPlanGb: Double = 150.0,
    val usedGb: Double = 74.2,
    val remainingGb: Double = 75.8,
    val billingCycleStart: String = "Oct 01, 2026",
    val billingCycleEnd: String = "Oct 31, 2026",
    val daysRemainingInCycle: Int = 18,
    val dailyAverageGb: Double = 5.7,
    val projectedMonthEndGb: Double = 138.4,
    val isDemoData: Boolean = true
) {
    val usagePercentage: Float
        get() = ((usedGb / totalPlanGb) * 100).toFloat().coerceIn(0f, 100f)

    val remainingPercentage: Float
        get() = ((remainingGb / totalPlanGb) * 100).toFloat().coerceIn(0f, 100f)
}
