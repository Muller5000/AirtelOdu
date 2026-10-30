package com.example.airteldatamonitor.data.model

enum class DeviceConnectionType(val label: String) {
    WIFI_5GHZ("5 GHz Wi-Fi"),
    WIFI_2_4GHZ("2.4 GHz Wi-Fi"),
    ETHERNET("LAN / Ethernet")
}

enum class DeviceStatus(val label: String) {
    CONNECTED("Active"),
    IDLE("Idle"),
    OFFLINE("Offline")
}

enum class DeviceCategory {
    SMARTPHONE,
    LAPTOP,
    SMART_TV,
    GAMING_CONSOLE,
    TABLET,
    SMART_SPEAKER,
    IOT_DEVICE,
    WORKSTATION
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val ipAddress: String,
    val macAddress: String,
    val connectionType: DeviceConnectionType,
    val status: DeviceStatus,
    val downloadSpeedMbps: Double? = null,
    val uploadSpeedMbps: Double? = null,
    val dataConsumedGb: Double? = null,
    val category: DeviceCategory,
    val lastActiveFormatted: String = "Active now",
    val isDemoData: Boolean = true,
    val rssi: Int? = null,
    val ssid: String? = null,
    val customNickname: String? = null,
    val isBlocked: Boolean = false,
    val isNewDevice: Boolean = false
) {
    val displayName: String
        get() = customNickname?.takeIf { it.isNotBlank() } ?: name
}
