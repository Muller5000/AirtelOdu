package com.example.airteldatamonitor.data.repository

import android.content.Context
import com.example.airteldatamonitor.data.local.AppPreferences
import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.RouterConnectionStatus
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.Throughput
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.model.UsageHistory
import com.example.airteldatamonitor.data.model.UsagePeriod
import com.example.airteldatamonitor.data.router.AirtelRouterApi
import com.example.airteldatamonitor.data.router.DemoRouterApiClient
import com.example.airteldatamonitor.data.router.ZltW154RRouterApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RouterRepository(
    private val demoApiClient: DemoRouterApiClient = DemoRouterApiClient(),
    private val zltApiClient: ZltW154RRouterApiClient = ZltW154RRouterApiClient(),
    private val context: Context? = null
) {
    private val appPreferences: AppPreferences? = context?.let { AppPreferences(it) }

    private val _settings = MutableStateFlow(appPreferences?.loadSettings() ?: RouterSettings())
    val settings: StateFlow<RouterSettings> = _settings.asStateFlow()

    val cachedUsage: DataUsage?
        get() = appPreferences?.loadCachedUsage()

    val cachedRouterInfo: RouterInfo?
        get() = appPreferences?.loadCachedRouterInfo()

    private val _connectionStatus = MutableStateFlow(RouterConnectionStatus.CONNECTED)
    val connectionStatus: StateFlow<RouterConnectionStatus> = _connectionStatus.asStateFlow()

    private val activeApi: AirtelRouterApi
        get() = if (_settings.value.isLiveRouterMode) zltApiClient else demoApiClient

    init {
        zltApiClient.updateCredentials(
            newHost = _settings.value.routerIp,
            newPort = _settings.value.httpPort,
            newPass = _settings.value.adminPasswordMasked
        )
    }

    private fun persistSettings() {
        appPreferences?.saveSettings(_settings.value)
    }

    fun setSimulationMode(mode: DemoSimulationMode) {
        demoApiClient.simulationMode = mode
        _settings.value = _settings.value.copy(simulationMode = mode)
        persistSettings()
    }

    fun setLiveRouterMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(isLiveRouterMode = enabled)
        if (!enabled) {
            _connectionStatus.value = RouterConnectionStatus.CONNECTED
        }
        persistSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDarkMode = enabled)
        persistSettings()
    }

    fun updateDataPlan(name: String, totalGb: Double) {
        val clampedGb = totalGb.coerceAtLeast(1.0)
        _settings.value = _settings.value.copy(
            planName = name.ifBlank { "Airtel Broadband" },
            monthlyPlanGb = clampedGb
        )
        persistSettings()
    }

    fun updateSettings(newSettings: RouterSettings) {
        _settings.value = newSettings
        demoApiClient.simulationMode = newSettings.simulationMode
        zltApiClient.updateCredentials(
            newHost = newSettings.routerIp,
            newPort = newSettings.httpPort,
            newPass = newSettings.adminPasswordMasked
        )
        persistSettings()
    }

    suspend fun testConnection(): Result<RouterConnectionStatus> {
        _connectionStatus.value = RouterConnectionStatus.CONNECTING
        val result = activeApi.testConnection(_settings.value.routerIp, _settings.value.httpPort)
        _connectionStatus.value = if (result.isSuccess) {
            result.getOrDefault(RouterConnectionStatus.CONNECTED)
        } else {
            RouterConnectionStatus.UNREACHABLE
        }
        return result
    }

    suspend fun fetchRouterInfo(): UiState<RouterInfo> {
        val result = activeApi.getRouterInfo()
        return if (result.isSuccess) {
            val info = result.getOrThrow()
            appPreferences?.saveCachedRouterInfo(info)
            _connectionStatus.value = RouterConnectionStatus.CONNECTED
            UiState.Success(info)
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Unknown error"
            updateErrorStatus(msg)
            UiState.Error(msg)
        }
    }

    suspend fun fetchDataUsage(): UiState<DataUsage> {
        val result = activeApi.getDataUsage()
        return if (result.isSuccess) {
            val raw = result.getOrThrow()
            val planLimit = _settings.value.monthlyPlanGb
            val planName = _settings.value.planName
            val remaining = (planLimit - raw.usedGb).coerceAtLeast(0.0)
            val adapted = raw.copy(
                planName = planName,
                totalPlanGb = planLimit,
                remainingGb = remaining
            )
            appPreferences?.saveCachedUsage(adapted)
            UiState.Success(adapted)
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to load data usage"
            updateErrorStatus(msg)
            UiState.Error(msg)
        }
    }

    private val customNicknames = mutableMapOf<String, String>()
    private val knownMacAddresses = mutableSetOf<String>()

    fun setDeviceNickname(mac: String, nickname: String) {
        val normMac = mac.trim().uppercase()
        if (nickname.isBlank()) {
            customNicknames.remove(normMac)
        } else {
            customNicknames[normMac] = nickname.trim()
        }
    }

    fun getDeviceNickname(mac: String): String? {
        return customNicknames[mac.trim().uppercase()]
    }

    suspend fun blockDevice(mac: String): Result<Boolean> {
        return activeApi.setDeviceBlocked(mac, true)
    }

    suspend fun unblockDevice(mac: String): Result<Boolean> {
        return activeApi.setDeviceBlocked(mac, false)
    }

    suspend fun fetchConnectedDevices(): UiState<List<ConnectedDevice>> {
        val result = activeApi.getConnectedDevices()
        return if (result.isSuccess) {
            val list = result.getOrThrow()
            if (list.isEmpty()) {
                UiState.Empty
            } else {
                val enriched = list.map { dev ->
                    val normMac = dev.macAddress.trim().uppercase()
                    val isNew = knownMacAddresses.isNotEmpty() && !knownMacAddresses.contains(normMac)
                    knownMacAddresses.add(normMac)
                    dev.copy(
                        customNickname = customNicknames[normMac],
                        isNewDevice = isNew
                    )
                }
                UiState.Success(enriched)
            }
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to fetch connected devices"
            updateErrorStatus(msg)
            UiState.Error(msg)
        }
    }

    suspend fun fetchThroughput(): UiState<Throughput> {
        val result = activeApi.getRealtimeThroughput()
        return if (result.isSuccess) {
            UiState.Success(result.getOrThrow())
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to read network activity"
            updateErrorStatus(msg)
            UiState.Error(msg)
        }
    }

    suspend fun fetchUsageHistory(period: UsagePeriod): UiState<UsageHistory> {
        val result = activeApi.getUsageHistory(period)
        return if (result.isSuccess) {
            val history = result.getOrThrow()
            if (history.dataPoints.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(history)
            }
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to read usage history"
            updateErrorStatus(msg)
            UiState.Error(msg)
        }
    }

    suspend fun fetchNetworkMode(): UiState<com.example.airteldatamonitor.data.model.NetworkMode> {
        val result = activeApi.getNetworkMode()
        return if (result.isSuccess) {
            UiState.Success(result.getOrThrow())
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to query network mode"
            UiState.Error(msg)
        }
    }

    suspend fun switchNetworkMode(mode: com.example.airteldatamonitor.data.model.NetworkMode): Result<Boolean> {
        return activeApi.setNetworkMode(mode)
    }

    suspend fun rebootRouter(): Result<Boolean> {
        val result = activeApi.rebootRouter()
        if (result.isSuccess && _settings.value.isLiveRouterMode) {
            _connectionStatus.value = RouterConnectionStatus.UNREACHABLE
        }
        return result
    }

    suspend fun fetchBandLockState(): UiState<com.example.airteldatamonitor.data.model.BandLockState> {
        val result = activeApi.getBandLockConfig()
        return if (result.isSuccess) {
            UiState.Success(result.getOrThrow())
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to read frequency bands from modem"
            UiState.Error(msg)
        }
    }

    suspend fun saveBandLockConfig(
        enable4gLock: Boolean,
        selected4gBands: List<Int>,
        enable5gLock: Boolean,
        selected5gBands: List<Int>
    ): Result<Boolean> {
        return activeApi.setBandLockConfig(enable4gLock, selected4gBands, enable5gLock, selected5gBands)
    }

    suspend fun fetchSmsMessages(folder: Int = 0): UiState<com.example.airteldatamonitor.data.model.SmsCenterState> {
        val result = activeApi.getSmsMessages(folder)
        return if (result.isSuccess) {
            UiState.Success(result.getOrThrow())
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Failed to read SMS messages from router SIM"
            UiState.Error(msg)
        }
    }

    suspend fun sendSms(phoneNo: String, content: String): Result<Boolean> {
        return activeApi.sendSms(phoneNo, content)
    }

    suspend fun deleteSms(id: String, isSent: Boolean = false): Result<Boolean> {
        return activeApi.deleteSms(id, isSent)
    }

    suspend fun markSmsAsRead(id: String): Result<Boolean> {
        return activeApi.markSmsAsRead(id)
    }

    suspend fun sendUssd(code: String): Result<com.example.airteldatamonitor.data.model.UssdResponse> {
        return activeApi.sendUssd(code)
    }

    private fun updateErrorStatus(errorMessage: String) {
        if (_settings.value.isLiveRouterMode) {
            if (errorMessage.contains("login failed", ignoreCase = true) || errorMessage.contains("password", ignoreCase = true)) {
                _connectionStatus.value = RouterConnectionStatus.AUTH_FAILED
            } else {
                _connectionStatus.value = RouterConnectionStatus.UNREACHABLE
            }
        }
    }
}
