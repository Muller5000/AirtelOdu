package com.example.airteldatamonitor.data.model

enum class DemoSimulationMode(val label: String, val description: String) {
    NORMAL("Normal Demo", "Shows realistic Airtel ZLT W154R Plus sample metrics"),
    LOADING("Simulate Loading", "Forces UI loading skeleton & spinner"),
    EMPTY("Simulate Empty", "Shows empty state (0 devices, no usage)"),
    ERROR("Simulate Error", "Simulates router connection failure")
}

data class RouterSettings(
    val routerIp: String = "192.168.1.1",
    val httpPort: Int = 80,
    val adminPasswordMasked: String = "admin",
    val refreshIntervalSec: Int = 10,
    val lowDataThresholdPercent: Int = 80,
    val enableDataAlerts: Boolean = true,
    val autoReconnect: Boolean = true,
    val simulationMode: DemoSimulationMode = DemoSimulationMode.NORMAL,
    val isLiveRouterMode: Boolean = true,
    val isDarkMode: Boolean = true,
    val monthlyPlanGb: Double = 150.0,
    val planName: String = "Airtel Broadband"
)
