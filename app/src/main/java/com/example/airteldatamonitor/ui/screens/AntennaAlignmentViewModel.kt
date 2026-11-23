package com.example.airteldatamonitor.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.SignalMetrics
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SignalHistorySample(
    val timestampSeconds: Long,
    val rsrpDbm: Int,
    val sinrDb: Double
)

class AntennaAlignmentViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    val settings: StateFlow<RouterSettings> = repository.settings

    private val _currentSignal = MutableStateFlow(
        repository.cachedRouterInfo?.signal ?: SignalMetrics(
            bars = 4,
            rsrpDbm = -81,
            rsrqDb = -9,
            sinrDb = 18.5,
            cellId = "404-45-72891",
            qualityLabel = "Excellent",
            pci = "404",
            frequencyMhz = "2600 MHz (B7)"
        )
    )
    val currentSignal: StateFlow<SignalMetrics> = _currentSignal.asStateFlow()

    private val _primaryBand = MutableStateFlow(repository.cachedRouterInfo?.primaryBand ?: "Band 7 (2600 MHz)")
    val primaryBand: StateFlow<String> = _primaryBand.asStateFlow()

    private val _modelName = MutableStateFlow(repository.cachedRouterInfo?.modelName ?: "Airtel ZLT W154R Plus")
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    private val _peakRsrpDbm = MutableStateFlow(_currentSignal.value.rsrpDbm)
    val peakRsrpDbm: StateFlow<Int> = _peakRsrpDbm.asStateFlow()

    private val _peakSinrDb = MutableStateFlow(_currentSignal.value.sinrDb)
    val peakSinrDb: StateFlow<Double> = _peakSinrDb.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    private val _isAudioToneEnabled = MutableStateFlow(false)
    val isAudioToneEnabled: StateFlow<Boolean> = _isAudioToneEnabled.asStateFlow()

    private val _historySamples = MutableStateFlow<List<SignalHistorySample>>(emptyList())
    val historySamples: StateFlow<List<SignalHistorySample>> = _historySamples.asStateFlow()

    private var pollingJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        initAudioTone()
    }

    private fun initAudioTone() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun toggleAuditing() {
        if (_isAuditing.value) {
            stopPolling()
        } else {
            startPolling()
        }
    }

    fun toggleAudioTone(enabled: Boolean) {
        _isAudioToneEnabled.value = enabled
        if (enabled && toneGenerator == null) {
            initAudioTone()
        }
    }

    fun resetPeaks() {
        _peakRsrpDbm.value = _currentSignal.value.rsrpDbm
        _peakSinrDb.value = _currentSignal.value.sinrDb
        _historySamples.value = emptyList()
    }

    fun startPolling() {
        _isAuditing.value = true
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchSignalSample()
                delay(1000) // Fast 1-second interval for real-time antenna alignment feedback
            }
        }
    }

    fun stopPolling() {
        _isAuditing.value = false
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun fetchSignalSample() {
        val result = repository.fetchRouterInfo()
        if (result is UiState.Success) {
            val info = result.data
            val sig = info.signal
            _currentSignal.value = sig
            _primaryBand.value = info.primaryBand
            _modelName.value = info.modelName

            // Track peak signals (closer to 0 is higher for dBm, higher is better for SINR)
            if (sig.rsrpDbm > _peakRsrpDbm.value) {
                _peakRsrpDbm.value = sig.rsrpDbm
            }
            if (sig.sinrDb > _peakSinrDb.value) {
                _peakSinrDb.value = sig.sinrDb
            }

            // Rolling history (max 30 seconds)
            val nowSec = System.currentTimeMillis() / 1000
            val updatedHistory = (_historySamples.value + SignalHistorySample(nowSec, sig.rsrpDbm, sig.sinrDb))
                .takeLast(30)
            _historySamples.value = updatedHistory

            // Play audio alignment tone if ladder/roof mode enabled
            if (_isAudioToneEnabled.value) {
                playSignalBeep(sig.rsrpDbm)
            }
        }
    }

    private fun playSignalBeep(rsrpDbm: Int) {
        try {
            val tone = when {
                rsrpDbm >= -80 -> ToneGenerator.TONE_PROP_BEEP2 // High fast pitch: Optimal signal!
                rsrpDbm >= -92 -> ToneGenerator.TONE_PROP_BEEP  // Medium pitch: Good
                else -> ToneGenerator.TONE_PROP_ACK            // Low tone: Fair / Searching
            }
            toneGenerator?.startTone(tone, 80)
        } catch (_: Exception) {
            // Ignore audio interruptions
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
