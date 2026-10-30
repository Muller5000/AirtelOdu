package com.example.airteldatamonitor.data.router

import com.example.airteldatamonitor.data.model.CategoryUsage
import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.DeviceCategory
import com.example.airteldatamonitor.data.model.DeviceConnectionType
import com.example.airteldatamonitor.data.model.DeviceStatus
import com.example.airteldatamonitor.data.model.RouterConnectionStatus
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.SignalMetrics
import com.example.airteldatamonitor.data.model.Throughput
import com.example.airteldatamonitor.data.model.UsageDataPoint
import com.example.airteldatamonitor.data.model.UsageHistory
import com.example.airteldatamonitor.data.model.UsagePeriod
import kotlinx.coroutines.delay

/**
 * Milestone 1 implementation of [AirtelRouterApi].
 *
 * Provides realistic demo data specifically modeled after the Airtel ZLT W154R Plus
 * outdoor/indoor LTE-A CPE router.
 *
 * Important:
 * - All returned models have `isDemoData = true`.
 * - Does not invent live hardware values or make fake calls to unverified endpoints.
 * - Allows switching between Simulation Modes to test UI loading, empty, and error states.
 */
class DemoRouterApiClient : AirtelRouterApi {

    var simulationMode: DemoSimulationMode = DemoSimulationMode.NORMAL

    override suspend fun testConnection(host: String, port: Int): Result<RouterConnectionStatus> {
        delay(600) // Simulate network round-trip
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Could not connect to router at $host:$port. Ensure you are connected to the router's Wi-Fi network.")
            )
            DemoSimulationMode.LOADING -> {
                delay(3000)
                Result.success(RouterConnectionStatus.CONNECTED)
            }
            else -> Result.success(RouterConnectionStatus.CONNECTED)
        }
    }

    override suspend fun getRouterInfo(): Result<RouterInfo> {
        delay(350)
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Failed to read router telemetry from Airtel ZLT W154R Plus. Connection timed out.")
            )
            else -> Result.success(
                RouterInfo(
                    modelName = "Airtel ZLT W154R Plus",
                    hardwareRevision = "W154R-REV2",
                    firmwareVersion = "ZLT_W154R_PLUS_V2.14.0",
                    ipAddress = "192.168.8.1",
                    macAddress = "B4:FB:E4:29:A1:7C",
                    networkType = "4G+ / LTE-A (Cat 7)",
                    primaryBand = "Band 3 (1800 MHz) + B7 CA",
                    simStatus = "Active (Airtel Broadband)",
                    uptimeFormatted = "6d 14h 23m",
                    wifi24GhzSsid = "Airtel_W154R_2.4G",
                    wifi5GhzSsid = "Airtel_W154R_5G",
                    isWifi24Enabled = true,
                    isWifi5Enabled = true,
                    signal = SignalMetrics(
                        bars = 4,
                        rsrpDbm = -86,
                        rsrqDb = -9,
                        sinrDb = 18.5,
                        cellId = "404-45-72891",
                        qualityLabel = "Strong & Stable"
                    ),
                    isDemoData = true
                )
            )
        }
    }

    override suspend fun getDataUsage(): Result<DataUsage> {
        delay(300)
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Unable to fetch data quota from router. Check local network connection.")
            )
            DemoSimulationMode.EMPTY -> Result.success(
                DataUsage(
                    planName = "Airtel Ultra Unlimited 150GB",
                    totalPlanGb = 150.0,
                    usedGb = 0.0,
                    remainingGb = 150.0,
                    daysRemainingInCycle = 30,
                    dailyAverageGb = 0.0,
                    projectedMonthEndGb = 0.0,
                    isDemoData = true
                )
            )
            else -> Result.success(
                DataUsage(
                    planName = "Airtel Ultra Unlimited 150GB",
                    totalPlanGb = 150.0,
                    usedGb = 74.2,
                    remainingGb = 75.8,
                    billingCycleStart = "Oct 01, 2026",
                    billingCycleEnd = "Oct 31, 2026",
                    daysRemainingInCycle = 18,
                    dailyAverageGb = 5.7,
                    projectedMonthEndGb = 138.4,
                    isDemoData = true
                )
            )
        }
    }

    override suspend fun getConnectedDevices(): Result<List<ConnectedDevice>> {
        delay(400)
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Failed to enumerate connected devices from router DHCP table.")
            )
            DemoSimulationMode.EMPTY -> Result.success(emptyList())
            else -> Result.success(
                sampleDevices.map { device ->
                    device.copy(isBlocked = demoBlockedMacs.contains(device.macAddress.uppercase()))
                }
            )
        }
    }

    override suspend fun getRealtimeThroughput(): Result<Throughput> {
        delay(200)
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Real-time bandwidth stream unavailable.")
            )
            DemoSimulationMode.EMPTY -> Result.success(
                Throughput(
                    downloadSpeedMbps = 0.0,
                    uploadSpeedMbps = 0.0,
                    pingMs = 0,
                    peakDownloadTodayMbps = 0.0,
                    peakUploadTodayMbps = 0.0,
                    isDemoData = true
                )
            )
            else -> Result.success(
                Throughput(
                    downloadSpeedMbps = 24.8,
                    uploadSpeedMbps = 6.4,
                    pingMs = 21,
                    peakDownloadTodayMbps = 68.2,
                    peakUploadTodayMbps = 18.5,
                    isDemoData = true
                )
            )
        }
    }

    override suspend fun getUsageHistory(period: UsagePeriod): Result<UsageHistory> {
        delay(350)
        return when (simulationMode) {
            DemoSimulationMode.ERROR -> Result.failure(
                Exception("Historical telemetry not available.")
            )
            DemoSimulationMode.EMPTY -> Result.success(
                UsageHistory(
                    period = period,
                    dataPoints = emptyList(),
                    totalGb = 0.0,
                    peakLabel = "None",
                    peakGb = 0.0,
                    averageGb = 0.0,
                    categories = emptyList(),
                    isDemoData = true
                )
            )
            else -> when (period) {
                UsagePeriod.DAILY -> Result.success(dailyUsageHistory)
                UsagePeriod.WEEKLY -> Result.success(weeklyUsageHistory)
                UsagePeriod.MONTHLY -> Result.success(monthlyUsageHistory)
            }
        }
    }

    private var demoNetworkMode: com.example.airteldatamonitor.data.model.NetworkMode = com.example.airteldatamonitor.data.model.NetworkMode.FOUR_G_ONLY

    override suspend fun getNetworkMode(): Result<com.example.airteldatamonitor.data.model.NetworkMode> {
        delay(150)
        return Result.success(demoNetworkMode)
    }

    private val demoBlockedMacs = mutableSetOf<String>()

    override suspend fun setNetworkMode(mode: com.example.airteldatamonitor.data.model.NetworkMode): Result<Boolean> {
        delay(400)
        demoNetworkMode = mode
        return Result.success(true)
    }

    override suspend fun rebootRouter(): Result<Boolean> {
        delay(500)
        return Result.success(true)
    }

    override suspend fun getBlockedMacs(): Result<List<String>> {
        delay(200)
        return Result.success(demoBlockedMacs.toList())
    }

    override suspend fun setDeviceBlocked(mac: String, blocked: Boolean): Result<Boolean> {
        delay(300)
        if (blocked) {
            demoBlockedMacs.add(mac.uppercase())
        } else {
            demoBlockedMacs.remove(mac.uppercase())
        }
        return Result.success(true)
    }

    private var demo4gLockEnabled = false
    private var demo5gLockEnabled = false
    private var demoSelected4gBands = mutableSetOf(1, 3, 7, 8, 20, 28, 38, 41)
    private var demoSelected5gBands = mutableSetOf(41, 78)

    override suspend fun getBandLockConfig(): Result<com.example.airteldatamonitor.data.model.BandLockState> {
        delay(250)
        val supported4g = listOf(
            com.example.airteldatamonitor.data.model.BandOption(1, "Band 1", "2100 MHz", is5G = false, isSelected = demoSelected4gBands.contains(1)),
            com.example.airteldatamonitor.data.model.BandOption(3, "Band 3", "1800 MHz (High Speed)", is5G = false, isSelected = demoSelected4gBands.contains(3)),
            com.example.airteldatamonitor.data.model.BandOption(7, "Band 7", "2600 MHz (Ultra Capacity)", is5G = false, isSelected = demoSelected4gBands.contains(7)),
            com.example.airteldatamonitor.data.model.BandOption(8, "Band 8", "900 MHz", is5G = false, isSelected = demoSelected4gBands.contains(8)),
            com.example.airteldatamonitor.data.model.BandOption(20, "Band 20", "800 MHz (Long Range)", is5G = false, isSelected = demoSelected4gBands.contains(20)),
            com.example.airteldatamonitor.data.model.BandOption(28, "Band 28", "700 MHz (Strong Indoors)", is5G = false, isSelected = demoSelected4gBands.contains(28)),
            com.example.airteldatamonitor.data.model.BandOption(38, "Band 38", "2600 MHz TDD", is5G = false, isSelected = demoSelected4gBands.contains(38)),
            com.example.airteldatamonitor.data.model.BandOption(41, "Band 41", "2500 MHz TDD", is5G = false, isSelected = demoSelected4gBands.contains(41))
        )
        val supported5g = listOf(
            com.example.airteldatamonitor.data.model.BandOption(41, "n41", "2500 MHz", is5G = true, isSelected = demoSelected5gBands.contains(41)),
            com.example.airteldatamonitor.data.model.BandOption(78, "n78", "3500 MHz (Airtel C-Band 5G)", is5G = true, isSelected = demoSelected5gBands.contains(78))
        )
        val connectedBand = if (demo5gLockEnabled) "5G (n78) + 4G (Band 3)" else "Band 3 (1800 MHz)"
        return Result.success(
            com.example.airteldatamonitor.data.model.BandLockState(
                is4gLockEnabled = demo4gLockEnabled,
                is5gLockEnabled = demo5gLockEnabled,
                supported4gBands = supported4g,
                supported5gBands = supported5g,
                currentConnectedBand = connectedBand
            )
        )
    }

    override suspend fun setBandLockConfig(
        enable4gLock: Boolean,
        selected4gBands: List<Int>,
        enable5gLock: Boolean,
        selected5gBands: List<Int>
    ): Result<Boolean> {
        delay(400)
        demo4gLockEnabled = enable4gLock
        demo5gLockEnabled = enable5gLock
        demoSelected4gBands.clear()
        demoSelected4gBands.addAll(selected4gBands)
        demoSelected5gBands.clear()
        demoSelected5gBands.addAll(selected5gBands)
        return Result.success(true)
    }

    private val demoSmsMessages = mutableListOf(
        com.example.airteldatamonitor.data.model.RouterSmsMessage(
            id = "1",
            sender = "Airtel",
            content = "Dear Customer, your daily data usage on 7011973831 for today was 11039.44 MB. Remaining balance: 77.9 GB.",
            timestamp = System.currentTimeMillis() - 3600_000 * 2,
            dateStr = "2026/10/16 17:35:00",
            isRead = false
        ),
        com.example.airteldatamonitor.data.model.RouterSmsMessage(
            id = "2",
            sender = "AirtelERC",
            content = "TXN_ID: D261012.0039.220018, Bundle Amt: N25,000. Unlimited SmartConnect 150GB activated successfully. Valid for 30 days.",
            timestamp = System.currentTimeMillis() - 86400_000 * 4,
            dateStr = "2026/10/12 00:39:50",
            isRead = true
        ),
        com.example.airteldatamonitor.data.model.RouterSmsMessage(
            id = "3",
            sender = "AirtelOTP",
            content = "Your MyAirtel verification code is 849201. Valid for 10 minutes. Do not share your OTP with anyone.",
            timestamp = System.currentTimeMillis() - 3600_000 * 5,
            dateStr = "2026/10/16 14:40:12",
            isRead = false
        ),
        com.example.airteldatamonitor.data.model.RouterSmsMessage(
            id = "4",
            sender = "MEGA\$OFFER",
            content = "Get MORE with Airtel Unlimited SmartConnect! Enjoy unlimited internet for just N25,000/30 days. Click https://airtel.com.ng to activate.",
            timestamp = System.currentTimeMillis() - 86400_000 * 1,
            dateStr = "2026/10/15 15:39:29",
            isRead = true
        )
    )

    override suspend fun getSmsMessages(folder: Int): Result<com.example.airteldatamonitor.data.model.SmsCenterState> {
        delay(200)
        val unread = demoSmsMessages.count { !it.isRead }
        return Result.success(
            com.example.airteldatamonitor.data.model.SmsCenterState(
                messages = demoSmsMessages.toList(),
                totalCount = demoSmsMessages.size,
                unreadCount = unread,
                maxCapacity = 500,
                smscNumber = "+2348020000009"
            )
        )
    }

    override suspend fun sendSms(phoneNo: String, content: String): Result<Boolean> {
        delay(500)
        demoSmsMessages.add(
            0,
            com.example.airteldatamonitor.data.model.RouterSmsMessage(
                id = (demoSmsMessages.size + 1).toString(),
                sender = phoneNo,
                content = content,
                timestamp = System.currentTimeMillis(),
                dateStr = "Just now",
                isRead = true,
                isSelf = true
            )
        )
        return Result.success(true)
    }

    override suspend fun deleteSms(id: String, isSent: Boolean): Result<Boolean> {
        delay(200)
        if (id == "DELETE ALL") {
            demoSmsMessages.clear()
        } else {
            demoSmsMessages.removeAll { it.id == id }
        }
        return Result.success(true)
    }

    override suspend fun markSmsAsRead(id: String): Result<Boolean> {
        delay(150)
        if (id == "READ ALL") {
            val updated = demoSmsMessages.map { it.copy(isRead = true) }
            demoSmsMessages.clear()
            demoSmsMessages.addAll(updated)
        } else {
            val idx = demoSmsMessages.indexOfFirst { it.id == id }
            if (idx != -1) {
                demoSmsMessages[idx] = demoSmsMessages[idx].copy(isRead = true)
            }
        }
        return Result.success(true)
    }

    override suspend fun sendUssd(code: String): Result<com.example.airteldatamonitor.data.model.UssdResponse> {
        delay(800)
        val reply = when (code.trim()) {
            "*310#" -> "Main Balance: N1,450.50. Data Balance: 77.92 GB. Bonus: N500. Dial *312# to purchase data bundles."
            "*312#" -> "Airtel Data Plans:\n1. Unlimited SmartConnect\n2. Daily / Weekly Bundles\n3. Monthly Bundles\n4. Router Special\n5. Check Balance"
            "*323#" -> "Active Data: 77.92 GB remaining out of 150 GB Unlimited SmartConnect. Expires in 29 days."
            else -> "Airtel Service ($code):\nService request processed successfully."
        }
        return Result.success(
            com.example.airteldatamonitor.data.model.UssdResponse(
                success = true,
                message = reply,
                needReply = code.trim() == "*312#",
                rawCode = code
            )
        )
    }

    companion object {
        val sampleDevices = listOf(
            ConnectedDevice(
                id = "dev_1",
                name = "Living Room LG 4K TV",
                ipAddress = "192.168.8.102",
                macAddress = "A4:83:E7:2B:90:1C",
                connectionType = DeviceConnectionType.WIFI_5GHZ,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 11.2,
                uploadSpeedMbps = 0.8,
                dataConsumedGb = 22.4,
                category = DeviceCategory.SMART_TV,
                lastActiveFormatted = "Streaming 4K"
            ),
            ConnectedDevice(
                id = "dev_2",
                name = "MacBook Pro 16\"",
                ipAddress = "192.168.8.105",
                macAddress = "3C:06:30:19:B2:FF",
                connectionType = DeviceConnectionType.WIFI_5GHZ,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 7.4,
                uploadSpeedMbps = 3.6,
                dataConsumedGb = 18.9,
                category = DeviceCategory.LAPTOP,
                lastActiveFormatted = "Active now"
            ),
            ConnectedDevice(
                id = "dev_3",
                name = "Samsung Galaxy S24 Ultra",
                ipAddress = "192.168.8.108",
                macAddress = "70:89:CC:54:11:08",
                connectionType = DeviceConnectionType.WIFI_5GHZ,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 3.2,
                uploadSpeedMbps = 1.1,
                dataConsumedGb = 9.8,
                category = DeviceCategory.SMARTPHONE,
                lastActiveFormatted = "Active now"
            ),
            ConnectedDevice(
                id = "dev_4",
                name = "PlayStation 5 Console",
                ipAddress = "192.168.8.110",
                macAddress = "00:D9:D1:43:99:A2",
                connectionType = DeviceConnectionType.ETHERNET,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 1.8,
                uploadSpeedMbps = 0.4,
                dataConsumedGb = 12.6,
                category = DeviceCategory.GAMING_CONSOLE,
                lastActiveFormatted = "Standby Download"
            ),
            ConnectedDevice(
                id = "dev_5",
                name = "iPad Pro 11\"",
                ipAddress = "192.168.8.114",
                macAddress = "DC:A9:04:77:8E:51",
                connectionType = DeviceConnectionType.WIFI_5GHZ,
                status = DeviceStatus.IDLE,
                downloadSpeedMbps = 0.05,
                uploadSpeedMbps = 0.01,
                dataConsumedGb = 4.7,
                category = DeviceCategory.TABLET,
                lastActiveFormatted = "Idle (12m ago)"
            ),
            ConnectedDevice(
                id = "dev_6",
                name = "Amazon Echo Studio",
                ipAddress = "192.168.8.120",
                macAddress = "68:54:5A:F1:C0:3E",
                connectionType = DeviceConnectionType.WIFI_2_4GHZ,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 0.9,
                uploadSpeedMbps = 0.08,
                dataConsumedGb = 2.4,
                category = DeviceCategory.SMART_SPEAKER,
                lastActiveFormatted = "Audio Stream"
            ),
            ConnectedDevice(
                id = "dev_7",
                name = "Office Workstation PC",
                ipAddress = "192.168.8.125",
                macAddress = "D8:BB:C1:23:45:90",
                connectionType = DeviceConnectionType.ETHERNET,
                status = DeviceStatus.IDLE,
                downloadSpeedMbps = 0.1,
                uploadSpeedMbps = 0.02,
                dataConsumedGb = 3.1,
                category = DeviceCategory.WORKSTATION,
                lastActiveFormatted = "Idle (45m ago)"
            ),
            ConnectedDevice(
                id = "dev_8",
                name = "Smart Home Hub Gateway",
                ipAddress = "192.168.8.130",
                macAddress = "18:B4:30:E8:42:1F",
                connectionType = DeviceConnectionType.WIFI_2_4GHZ,
                status = DeviceStatus.CONNECTED,
                downloadSpeedMbps = 0.15,
                uploadSpeedMbps = 0.4,
                dataConsumedGb = 0.3,
                category = DeviceCategory.IOT_DEVICE,
                lastActiveFormatted = "Telemetry Sync"
            )
        )

        val dailyUsageHistory = UsageHistory(
            period = UsagePeriod.DAILY,
            dataPoints = listOf(
                UsageDataPoint("00:00", 0.4, 0.1),
                UsageDataPoint("03:00", 0.2, 0.05),
                UsageDataPoint("06:00", 0.6, 0.15),
                UsageDataPoint("09:00", 1.8, 0.5),
                UsageDataPoint("12:00", 2.3, 0.7),
                UsageDataPoint("15:00", 2.1, 0.6),
                UsageDataPoint("18:00", 3.4, 0.9),
                UsageDataPoint("21:00", 4.1, 1.1)
            ),
            totalGb = 18.4,
            peakLabel = "21:00 - 23:00",
            peakGb = 5.2,
            averageGb = 2.3,
            categories = listOf(
                CategoryUsage("Video Streaming (4K/HD)", 8.2, 44.5f),
                CategoryUsage("Work & Video Conferencing", 4.8, 26.1f),
                CategoryUsage("Gaming & Updates", 3.1, 16.8f),
                CategoryUsage("Web Browsing & Social", 1.5, 8.2f),
                CategoryUsage("Smart Home & Background", 0.8, 4.4f)
            ),
            isDemoData = true
        )

        val weeklyUsageHistory = UsageHistory(
            period = UsagePeriod.WEEKLY,
            dataPoints = listOf(
                UsageDataPoint("Mon", 4.8, 1.2),
                UsageDataPoint("Tue", 5.4, 1.5),
                UsageDataPoint("Wed", 6.1, 1.8),
                UsageDataPoint("Thu", 5.2, 1.3),
                UsageDataPoint("Fri", 7.8, 2.1),
                UsageDataPoint("Sat", 9.4, 2.6),
                UsageDataPoint("Sun", 8.9, 2.4)
            ),
            totalGb = 60.5,
            peakLabel = "Saturday",
            peakGb = 12.0,
            averageGb = 8.64,
            categories = listOf(
                CategoryUsage("Video Streaming (4K/HD)", 26.8, 44.3f),
                CategoryUsage("Work & Video Conferencing", 15.2, 25.1f),
                CategoryUsage("Gaming & Updates", 11.4, 18.8f),
                CategoryUsage("Web Browsing & Social", 4.6, 7.6f),
                CategoryUsage("Smart Home & Background", 2.5, 4.2f)
            ),
            isDemoData = true
        )

        val monthlyUsageHistory = UsageHistory(
            period = UsagePeriod.MONTHLY,
            dataPoints = listOf(
                UsageDataPoint("Week 1", 24.5, 6.2),
                UsageDataPoint("Week 2", 28.1, 7.4),
                UsageDataPoint("Week 3", 16.8, 4.3),
                UsageDataPoint("Week 4 (Proj.)", 22.0, 5.5)
            ),
            totalGb = 114.8,
            peakLabel = "Week 2",
            peakGb = 35.5,
            averageGb = 28.7,
            categories = listOf(
                CategoryUsage("Video Streaming (4K/HD)", 51.2, 44.6f),
                CategoryUsage("Work & Video Conferencing", 29.5, 25.7f),
                CategoryUsage("Gaming & Updates", 20.8, 18.1f),
                CategoryUsage("Web Browsing & Social", 8.9, 7.8f),
                CategoryUsage("Smart Home & Background", 4.4, 3.8f)
            ),
            isDemoData = true
        )
    }
}
