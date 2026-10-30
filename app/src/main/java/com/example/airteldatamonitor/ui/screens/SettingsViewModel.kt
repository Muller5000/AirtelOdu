package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.RouterConnectionStatus
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    val settings: StateFlow<RouterSettings> = repository.settings
    val connectionStatus: StateFlow<RouterConnectionStatus> = repository.connectionStatus

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _testFeedback = MutableStateFlow<String?>(null)
    val testFeedback: StateFlow<String?> = _testFeedback.asStateFlow()

    private val _currentNetworkMode = MutableStateFlow(com.example.airteldatamonitor.data.model.NetworkMode.FOUR_G_ONLY)
    val currentNetworkMode: StateFlow<com.example.airteldatamonitor.data.model.NetworkMode> = _currentNetworkMode.asStateFlow()

    private val _isSwitchingNetworkMode = MutableStateFlow(false)
    val isSwitchingNetworkMode: StateFlow<Boolean> = _isSwitchingNetworkMode.asStateFlow()

    private val _networkSwitchFeedback = MutableStateFlow<String?>(null)
    val networkSwitchFeedback: StateFlow<String?> = _networkSwitchFeedback.asStateFlow()

    private val _isRebooting = MutableStateFlow(false)
    val isRebooting: StateFlow<Boolean> = _isRebooting.asStateFlow()

    private val _rebootCountdown = MutableStateFlow(0)
    val rebootCountdown: StateFlow<Int> = _rebootCountdown.asStateFlow()

    private val _rebootFeedback = MutableStateFlow<String?>(null)
    val rebootFeedback: StateFlow<String?> = _rebootFeedback.asStateFlow()

    init {
        loadNetworkMode()
    }

    fun loadNetworkMode() {
        viewModelScope.launch {
            when (val res = repository.fetchNetworkMode()) {
                is com.example.airteldatamonitor.data.model.UiState.Success -> _currentNetworkMode.value = res.data
                else -> {}
            }
        }
    }

    fun changeNetworkMode(newMode: com.example.airteldatamonitor.data.model.NetworkMode) {
        viewModelScope.launch {
            _isSwitchingNetworkMode.value = true
            _networkSwitchFeedback.value = "Switching router network mode to ${newMode.title}..."
            val result = repository.switchNetworkMode(newMode)
            _isSwitchingNetworkMode.value = false
            if (result.isSuccess) {
                _currentNetworkMode.value = newMode
                _networkSwitchFeedback.value = "✓ Successfully switched network mode to ${newMode.title}"
            } else {
                val err = result.exceptionOrNull()?.message ?: "Command rejected by router"
                _networkSwitchFeedback.value = "✗ Failed to switch mode: $err"
            }
        }
    }

    fun rebootRouter() {
        viewModelScope.launch {
            _isRebooting.value = true
            _rebootFeedback.value = "Sending reboot command to router..."
            val result = repository.rebootRouter()
            if (result.isSuccess) {
                for (sec in 60 downTo 1) {
                    _rebootCountdown.value = sec
                    _rebootFeedback.value = "Router is restarting... Reconnecting in ${sec}s"
                    kotlinx.coroutines.delay(1000)
                }
                _rebootCountdown.value = 0
                _rebootFeedback.value = "Verifying router connection..."
                val testRes = repository.testConnection()
                _isRebooting.value = false
                if (testRes.isSuccess) {
                    _rebootFeedback.value = "✓ Router restarted successfully and is back online"
                    loadNetworkMode()
                } else {
                    _rebootFeedback.value = "Router rebooted. Reconnect to Wi-Fi if needed."
                }
            } else {
                _isRebooting.value = false
                val err = result.exceptionOrNull()?.message ?: "Reboot command rejected"
                _rebootFeedback.value = "✗ Failed to reboot router: $err"
            }
        }
    }

    fun updateRouterIp(ip: String) {
        repository.updateSettings(settings.value.copy(routerIp = ip))
    }

    fun updateHttpPort(port: Int) {
        repository.updateSettings(settings.value.copy(httpPort = port))
    }

    fun updatePassword(pass: String) {
        repository.updateSettings(settings.value.copy(adminPasswordMasked = pass))
    }

    fun updateRefreshInterval(seconds: Int) {
        repository.updateSettings(settings.value.copy(refreshIntervalSec = seconds))
    }

    fun updateLowDataThreshold(threshold: Int) {
        repository.updateSettings(settings.value.copy(lowDataThresholdPercent = threshold))
    }

    fun toggleDataAlerts(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(enableDataAlerts = enabled))
    }

    fun toggleLiveRouterMode(enabled: Boolean) {
        repository.setLiveRouterMode(enabled)
    }

    fun toggleDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }

    fun updateDataPlan(name: String, totalGb: Double) {
        repository.updateDataPlan(name, totalGb)
    }

    fun setSimulationMode(mode: DemoSimulationMode) {
        repository.setSimulationMode(mode)
    }

    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testFeedback.value = if (settings.value.isLiveRouterMode) {
                "Connecting to Airtel ZLT W154R Plus at ${settings.value.routerIp}:${settings.value.httpPort}..."
            } else {
                "Testing connection in Demo mode..."
            }
            val result = repository.testConnection()
            _isTestingConnection.value = false
            _testFeedback.value = if (result.isSuccess) {
                val status = result.getOrDefault(RouterConnectionStatus.CONNECTED)
                if (status == RouterConnectionStatus.AUTH_FAILED) {
                    "✗ Authentication failed: Invalid router password. Check Settings."
                } else {
                    "✓ Connected: Router responded at ${settings.value.routerIp}:${settings.value.httpPort}"
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Check Wi-Fi network"
                "✗ Connection failed: $errorMsg"
            }
        }
    }
}
