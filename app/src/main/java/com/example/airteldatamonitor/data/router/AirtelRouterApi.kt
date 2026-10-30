package com.example.airteldatamonitor.data.router

import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.RouterConnectionStatus
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.Throughput
import com.example.airteldatamonitor.data.model.UsageHistory
import com.example.airteldatamonitor.data.model.UsagePeriod

/**
 * Interface contract for communicating with an Airtel router.
 *
 * This abstraction separates the Jetpack Compose UI from the network/router
 * transport layer, allowing the app to run with realistic sample data during
 * Milestone 1 and plug in actual HTTP/CGI router calls in Milestone 2
 * once physical router hardware is tested.
 */
interface AirtelRouterApi {
    /**
     * Pings or checks accessibility of the router at the specified host and port.
     */
    suspend fun testConnection(host: String, port: Int): Result<RouterConnectionStatus>

    /**
     * Retrieves router hardware info, firmware version, signal levels (RSRP, SINR),
     * and wireless network state.
     */
    suspend fun getRouterInfo(): Result<RouterInfo>

    /**
     * Retrieves cellular broadband data quota, used bytes, and billing cycle dates.
     */
    suspend fun getDataUsage(): Result<DataUsage>

    /**
     * Retrieves the list of currently connected DHCP/Wi-Fi clients.
     * Note: Per-device traffic telemetry depends on firmware capability.
     */
    suspend fun getConnectedDevices(): Result<List<ConnectedDevice>>

    /**
     * Retrieves current real-time network throughput (download and upload Mbps).
     */
    suspend fun getRealtimeThroughput(): Result<Throughput>

    /**
     * Retrieves historical consumption breakdown for daily, weekly, or monthly periods.
     */
    suspend fun getUsageHistory(period: UsagePeriod): Result<UsageHistory>

    /**
     * Retrieves current cellular network mode (e.g. 5G/4G Auto, 4G Only).
     */
    suspend fun getNetworkMode(): Result<com.example.airteldatamonitor.data.model.NetworkMode>

    /**
     * Changes cellular network mode preference on the router modem.
     */
    suspend fun setNetworkMode(mode: com.example.airteldatamonitor.data.model.NetworkMode): Result<Boolean>

    /**
     * Triggers a remote reboot of the router hardware.
     */
    suspend fun rebootRouter(): Result<Boolean>

    /**
     * Retrieves the list of blocked MAC addresses from the router firewall.
     */
    suspend fun getBlockedMacs(): Result<List<String>>

    /**
     * Toggles block status for a device MAC address on the router firewall.
     */
    suspend fun setDeviceBlocked(mac: String, blocked: Boolean): Result<Boolean>

    /**
     * Retrieves the supported and currently locked 4G and 5G frequency bands.
     */
    suspend fun getBandLockConfig(): Result<com.example.airteldatamonitor.data.model.BandLockState>

    /**
     * Updates locked frequency bands on the router cellular modem.
     */
    suspend fun setBandLockConfig(
        enable4gLock: Boolean,
        selected4gBands: List<Int>,
        enable5gLock: Boolean,
        selected5gBands: List<Int>
    ): Result<Boolean>

    /**
     * Retrieves SMS messages from the router SIM inbox (folder = 0) or sent box (folder = 1).
     */
    suspend fun getSmsMessages(folder: Int = 0): Result<com.example.airteldatamonitor.data.model.SmsCenterState>

    /**
     * Sends an SMS message from the router's cellular SIM card to the specified phone number.
     */
    suspend fun sendSms(phoneNo: String, content: String): Result<Boolean>

    /**
     * Deletes one or more SMS messages by ID from the router SIM memory.
     */
    suspend fun deleteSms(id: String, isSent: Boolean = false): Result<Boolean>

    /**
     * Marks SMS message(s) as read. Pass "READ ALL" to mark all messages as read.
     */
    suspend fun markSmsAsRead(id: String): Result<Boolean>

    /**
     * Executes a USSD service request (e.g. *312# or *310#) on the cellular network.
     */
    suspend fun sendUssd(code: String): Result<com.example.airteldatamonitor.data.model.UssdResponse>
}
