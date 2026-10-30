package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.Throughput
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiData(
    val routerInfo: RouterInfo,
    val dataUsage: DataUsage,
    val throughput: Throughput,
    val connectedDeviceCount: Int
)

class HomeViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    val settings: StateFlow<RouterSettings> = repository.settings

    private val _uiState = MutableStateFlow<UiState<HomeUiData>>(
        run {
            val cachedUsage = repository.cachedUsage
            val cachedInfo = repository.cachedRouterInfo
            if (cachedUsage != null && cachedInfo != null) {
                UiState.Success(
                    HomeUiData(
                        routerInfo = cachedInfo,
                        dataUsage = cachedUsage,
                        throughput = Throughput(),
                        connectedDeviceCount = 0
                    )
                )
            } else {
                UiState.Loading
            }
        }
    )
    val uiState: StateFlow<UiState<HomeUiData>> = _uiState.asStateFlow()

    init {
        loadData(forceReload = false)
        viewModelScope.launch {
            repository.settings.collect { newSettings ->
                val current = _uiState.value
                if (current is UiState.Success) {
                    val updatedUsage = current.data.dataUsage.copy(
                        planName = newSettings.planName,
                        totalPlanGb = newSettings.monthlyPlanGb,
                        remainingGb = (newSettings.monthlyPlanGb - current.data.dataUsage.usedGb).coerceAtLeast(0.0)
                    )
                    _uiState.value = UiState.Success(current.data.copy(dataUsage = updatedUsage))
                }
            }
        }
    }

    fun loadData(forceReload: Boolean = false) {
        viewModelScope.launch {
            if (forceReload || _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }

            val infoResult = repository.fetchRouterInfo()
            val usageResult = repository.fetchDataUsage()
            val throughputResult = repository.fetchThroughput()
            val devicesResult = repository.fetchConnectedDevices()

            val isExplicitError = repository.settings.value.simulationMode == DemoSimulationMode.ERROR
            if (infoResult is UiState.Error && (forceReload || isExplicitError || _uiState.value !is UiState.Success)) {
                _uiState.value = UiState.Error(infoResult.message)
                return@launch
            }
            if (usageResult is UiState.Error && (forceReload || isExplicitError || _uiState.value !is UiState.Success)) {
                _uiState.value = UiState.Error(usageResult.message)
                return@launch
            }

            val routerInfo = (infoResult as? UiState.Success)?.data
                ?: (_uiState.value as? UiState.Success)?.data?.routerInfo
                ?: repository.cachedRouterInfo
                ?: RouterInfo()

            val dataUsage = (usageResult as? UiState.Success)?.data
                ?: (_uiState.value as? UiState.Success)?.data?.dataUsage
                ?: repository.cachedUsage
                ?: DataUsage()

            val throughput = (throughputResult as? UiState.Success)?.data
                ?: (_uiState.value as? UiState.Success)?.data?.throughput
                ?: Throughput()

            val deviceCount = when (devicesResult) {
                is UiState.Success -> devicesResult.data.size
                is UiState.Empty -> 0
                else -> (_uiState.value as? UiState.Success)?.data?.connectedDeviceCount ?: 0
            }

            if (usageResult is UiState.Empty && deviceCount == 0 && _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Empty
            } else {
                _uiState.value = UiState.Success(
                    HomeUiData(
                        routerInfo = routerInfo,
                        dataUsage = dataUsage,
                        throughput = throughput,
                        connectedDeviceCount = deviceCount
                    )
                )
            }
        }
    }

    fun switchSimulationMode(mode: DemoSimulationMode) {
        repository.setSimulationMode(mode)
        loadData(forceReload = true)
    }

    fun updateDataPlan(name: String, totalGb: Double) {
        repository.updateDataPlan(name, totalGb)
    }
}
