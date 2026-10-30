package com.example.airteldatamonitor.data.model

data class SignalMetrics(
    val bars: Int, // 1 to 5
    val rsrpDbm: Int, // e.g. -86 dBm
    val rsrqDb: Int, // e.g. -9 dB
    val sinrDb: Double, // e.g. 18.5 dB
    val cellId: String = "404-45-72891",
    val qualityLabel: String = "Good"
)

data class RouterInfo(
    val modelName: String = "Airtel ZLT W154R Plus",
    val hardwareRevision: String = "W154R-REV2",
    val firmwareVersion: String = "ZLT_W154R_PLUS_V2.14.0",
    val ipAddress: String = "192.168.8.1",
    val macAddress: String = "B4:FB:E4:29:A1:7C",
    val networkType: String = "4G+ / LTE-A (Cat 7)",
    val primaryBand: String = "Band 3 (1800 MHz) + B7 CA",
    val simStatus: String = "Active (Airtel Broadband)",
    val uptimeFormatted: String = "6d 14h 23m",
    val wifi24GhzSsid: String = "Airtel_W154R_2.4G",
    val wifi5GhzSsid: String = "Airtel_W154R_5G",
    val isWifi24Enabled: Boolean = true,
    val isWifi5Enabled: Boolean = true,
    val signal: SignalMetrics = SignalMetrics(
        bars = 4,
        rsrpDbm = -86,
        rsrqDb = -9,
        sinrDb = 18.5
    ),
    val isDemoData: Boolean = true
)
