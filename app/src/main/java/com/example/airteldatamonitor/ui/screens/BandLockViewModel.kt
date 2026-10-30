package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.BandLockState
import com.example.airteldatamonitor.data.model.BandOption
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BandPreset {
    MAX_SPEED,          // Locks Band 3 + Band 7 + n78 (Ultra high throughput)
    STRONGEST_INDOORS,  // Locks Band 20 + Band 28 (Penetrates walls, long range)
    AUTO_ALL_BANDS      // Disables locking, allows modem to use all supported bands
}

class BandLockViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<BandLockState>>(UiState.Loading)
    val uiState: StateFlow<UiState<BandLockState>> = _uiState.asStateFlow()

    private val _is4gLockEnabled = MutableStateFlow(false)
    val is4gLockEnabled: StateFlow<Boolean> = _is4gLockEnabled.asStateFlow()

    private val _is5gLockEnabled = MutableStateFlow(false)
    val is5gLockEnabled: StateFlow<Boolean> = _is5gLockEnabled.asStateFlow()

    private val _selected4gBands = MutableStateFlow<Set<Int>>(emptySet())
    val selected4gBands: StateFlow<Set<Int>> = _selected4gBands.asStateFlow()

    private val _selected5gBands = MutableStateFlow<Set<Int>>(emptySet())
    val selected5gBands: StateFlow<Set<Int>> = _selected5gBands.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    init {
        loadBandLockState()
    }

    fun loadBandLockState() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val state = repository.fetchBandLockState()
            _uiState.value = state

            if (state is UiState.Success) {
                _is4gLockEnabled.value = state.data.is4gLockEnabled
                _is5gLockEnabled.value = state.data.is5gLockEnabled
                _selected4gBands.value = state.data.supported4gBands.filter { it.isSelected }.map { it.bandNumber }.toSet()
                _selected5gBands.value = state.data.supported5gBands.filter { it.isSelected }.map { it.bandNumber }.toSet()
            }
        }
    }

    fun toggle4gLock(enabled: Boolean) {
        _is4gLockEnabled.value = enabled
        if (enabled && _selected4gBands.value.isEmpty()) {
            val success = _uiState.value as? UiState.Success
            val all4g = success?.data?.supported4gBands?.map { it.bandNumber }?.toSet() ?: setOf(3)
            _selected4gBands.value = all4g
        }
    }

    fun toggle5gLock(enabled: Boolean) {
        _is5gLockEnabled.value = enabled
        if (enabled && _selected5gBands.value.isEmpty()) {
            val success = _uiState.value as? UiState.Success
            val all5g = success?.data?.supported5gBands?.map { it.bandNumber }?.toSet() ?: setOf(78)
            _selected5gBands.value = all5g
        }
    }

    fun toggleBand(bandNumber: Int, is5G: Boolean) {
        if (is5G) {
            val current = _selected5gBands.value.toMutableSet()
            if (current.contains(bandNumber)) {
                if (current.size > 1) current.remove(bandNumber)
            } else {
                current.add(bandNumber)
            }
            _selected5gBands.value = current
        } else {
            val current = _selected4gBands.value.toMutableSet()
            if (current.contains(bandNumber)) {
                if (current.size > 1) current.remove(bandNumber)
            } else {
                current.add(bandNumber)
            }
            _selected4gBands.value = current
        }
    }

    fun applyPreset(preset: BandPreset) {
        val success = _uiState.value as? UiState.Success ?: return
        val supported4g = success.data.supported4gBands.map { it.bandNumber }.toSet()
        val supported5g = success.data.supported5gBands.map { it.bandNumber }.toSet()

        when (preset) {
            BandPreset.MAX_SPEED -> {
                _is4gLockEnabled.value = true
                _is5gLockEnabled.value = true
                // Prefer B3 and B7 on 4G, n78 on 5G
                val target4g = setOf(3, 7).filter { supported4g.contains(it) }.toSet()
                _selected4gBands.value = target4g.ifEmpty { setOf(3) }
                val target5g = setOf(78).filter { supported5g.contains(it) }.toSet()
                _selected5gBands.value = target5g.ifEmpty { supported5g }
                _actionFeedback.value = "Preset applied: Max Speed (B3, B7, n78). Tap 'Apply to Router' to commit."
            }
            BandPreset.STRONGEST_INDOORS -> {
                _is4gLockEnabled.value = true
                _is5gLockEnabled.value = false
                val target4g = setOf(20, 28).filter { supported4g.contains(it) }.toSet()
                _selected4gBands.value = target4g.ifEmpty { setOf(28) }
                _actionFeedback.value = "Preset applied: Strongest Indoors (B20, B28). Tap 'Apply to Router' to commit."
            }
            BandPreset.AUTO_ALL_BANDS -> {
                _is4gLockEnabled.value = false
                _is5gLockEnabled.value = false
                _selected4gBands.value = supported4g
                _selected5gBands.value = supported5g
                _actionFeedback.value = "Preset applied: All Bands (Auto). Tap 'Apply to Router' to commit."
            }
        }
    }

    fun applyChanges() {
        viewModelScope.launch {
            if (_is4gLockEnabled.value && _selected4gBands.value.isEmpty()) {
                _actionFeedback.value = "✗ Please select at least one 4G band"
                return@launch
            }
            if (_is5gLockEnabled.value && _selected5gBands.value.isEmpty()) {
                _actionFeedback.value = "✗ Please select at least one 5G band"
                return@launch
            }

            _isApplying.value = true
            _actionFeedback.value = "Applying band lock configuration to modem..."

            val result = repository.saveBandLockConfig(
                enable4gLock = _is4gLockEnabled.value,
                selected4gBands = _selected4gBands.value.toList(),
                enable5gLock = _is5gLockEnabled.value,
                selected5gBands = _selected5gBands.value.toList()
            )

            _isApplying.value = false
            if (result.isSuccess) {
                _actionFeedback.value = "✓ Frequency bands successfully locked on router modem!"
                loadBandLockState()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to lock frequency bands"
                _actionFeedback.value = "✗ $err"
            }
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }
}
