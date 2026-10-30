package com.example.airteldatamonitor.data.model

data class Throughput(
    val downloadSpeedMbps: Double = 24.8,
    val uploadSpeedMbps: Double = 6.4,
    val pingMs: Int = 22,
    val peakDownloadTodayMbps: Double = 68.2,
    val peakUploadTodayMbps: Double = 18.5,
    val isDemoData: Boolean = true
)
