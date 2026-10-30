package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.repository.RouterRepository
import com.example.airteldatamonitor.data.speedtest.SpeedTestEngine
import com.example.airteldatamonitor.data.speedtest.SpeedTestStage
import com.example.airteldatamonitor.data.speedtest.SpeedTestState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpeedTestViewModel(
    private val repository: RouterRepository,
    private val speedTestEngine: SpeedTestEngine = SpeedTestEngine()
) : ViewModel() {

    val testState: StateFlow<SpeedTestState> = speedTestEngine.testState
    val settings: StateFlow<RouterSettings> = repository.settings

    private val _routerInfo = MutableStateFlow<RouterInfo?>(null)
    val routerInfo: StateFlow<RouterInfo?> = _routerInfo.asStateFlow()

    private var activeTestJob: Job? = null

    init {
        viewModelScope.launch {
            when (val res = repository.fetchRouterInfo()) {
                is UiState.Success -> _routerInfo.value = res.data
                else -> {}
            }
        }
    }

    fun startSpeedTest() {
        if (activeTestJob?.isActive == true) return
        activeTestJob = viewModelScope.launch {
            speedTestEngine.runSpeedTest()
        }
    }

    fun cancelSpeedTest() {
        activeTestJob?.cancel()
        activeTestJob = null
        speedTestEngine.reset()
    }

    override fun onCleared() {
        super.onCleared()
        cancelSpeedTest()
    }
}
