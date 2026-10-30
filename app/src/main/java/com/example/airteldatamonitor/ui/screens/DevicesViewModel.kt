package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DeviceConnectionType
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DeviceFilter(val label: String) {
    ALL("All Devices"),
    WIFI_5GHZ("5 GHz Wi-Fi"),
    WIFI_2_4GHZ("2.4 GHz Wi-Fi"),
    LAN("LAN / Ethernet")
}

class DevicesViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    val settings: StateFlow<RouterSettings> = repository.settings

    private val _uiState = MutableStateFlow<UiState<List<ConnectedDevice>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ConnectedDevice>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DeviceFilter.ALL)
    val selectedFilter: StateFlow<DeviceFilter> = _selectedFilter.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private val _isOperating = MutableStateFlow<String?>(null)
    val isOperating: StateFlow<String?> = _isOperating.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect {
                loadDevices()
            }
        }
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repository.fetchConnectedDevices()
        }
    }

    fun toggleBlockDevice(device: ConnectedDevice) {
        viewModelScope.launch {
            _isOperating.value = device.macAddress
            val targetBlocked = !device.isBlocked
            val actionName = if (targetBlocked) "Blocking" else "Unblocking"
            _actionFeedback.value = "$actionName ${device.displayName}..."

            val result = if (targetBlocked) {
                repository.blockDevice(device.macAddress)
            } else {
                repository.unblockDevice(device.macAddress)
            }

            _isOperating.value = null
            if (result.isSuccess) {
                val stateText = if (targetBlocked) "blocked from router access" else "unblocked"
                _actionFeedback.value = "✓ ${device.displayName} has been $stateText"
                _uiState.value = repository.fetchConnectedDevices()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Operation rejected by router"
                _actionFeedback.value = "✗ Failed to update device: $err"
            }
        }
    }

    fun renameDevice(device: ConnectedDevice, newNickname: String) {
        repository.setDeviceNickname(device.macAddress, newNickname)
        _actionFeedback.value = "✓ Nickname updated to \"${newNickname.ifBlank { device.name }}\""
        viewModelScope.launch {
            _uiState.value = repository.fetchConnectedDevices()
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: DeviceFilter) {
        _selectedFilter.value = filter
    }

    fun filterDevices(devices: List<ConnectedDevice>): List<ConnectedDevice> {
        val query = _searchQuery.value.trim().lowercase()
        val filter = _selectedFilter.value

        return devices.filter { device ->
            val matchesFilter = when (filter) {
                DeviceFilter.ALL -> true
                DeviceFilter.WIFI_5GHZ -> device.connectionType == DeviceConnectionType.WIFI_5GHZ
                DeviceFilter.WIFI_2_4GHZ -> device.connectionType == DeviceConnectionType.WIFI_2_4GHZ
                DeviceFilter.LAN -> device.connectionType == DeviceConnectionType.ETHERNET
            }

            val matchesQuery = query.isEmpty() ||
                device.displayName.lowercase().contains(query) ||
                device.name.lowercase().contains(query) ||
                device.ipAddress.contains(query) ||
                device.macAddress.lowercase().contains(query)

            matchesFilter && matchesQuery
        }
    }
}
