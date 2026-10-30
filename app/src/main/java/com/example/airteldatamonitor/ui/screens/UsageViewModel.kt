package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.model.UsageHistory
import com.example.airteldatamonitor.data.model.UsagePeriod
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsageViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    val settings: StateFlow<RouterSettings> = repository.settings

    private val _uiState = MutableStateFlow<UiState<UsageHistory>>(UiState.Loading)
    val uiState: StateFlow<UiState<UsageHistory>> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(UsagePeriod.DAILY)
    val selectedPeriod: StateFlow<UsagePeriod> = _selectedPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect {
                loadHistory(_selectedPeriod.value)
            }
        }
    }

    fun selectPeriod(period: UsagePeriod) {
        _selectedPeriod.value = period
        loadHistory(period)
    }

    fun loadHistory(period: UsagePeriod = _selectedPeriod.value) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repository.fetchUsageHistory(period)
        }
    }
}
